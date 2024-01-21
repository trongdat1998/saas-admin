package io.bhex.saas.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import io.bhex.base.account.*;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.base.proto.BaseRequest;
import io.bhex.bhop.common.dto.param.BalanceDetailDTO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.grpc.client.AccountAssetClient;
import io.bhex.bhop.common.grpc.client.BhAccountClient;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.util.JsonUtil;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.SaasTransferRecordDTO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.AddSaasTransferPO;
import io.bhex.saas.admin.dao.SaasTransferRecordMapper;
import io.bhex.saas.admin.grpc.client.impl.GrpcBalanceClient;
import io.bhex.saas.admin.model.SaasTransferRecord;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.util.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SaasTransferService {

    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private AccountAssetClient assetClient;
    @Autowired
    private BhAccountClient accountClient;
    @Autowired
    private SaasTransferRecordMapper transferRecordMapper;
    @Autowired
    private SnowflakeGenerator idGenerator;
    @Autowired
    private GrpcBalanceClient grpcBalanceClient;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;



    public ResultModel addRecord(AddSaasTransferPO po, AdminUserReply adminUser, boolean submit) {

        //check account balance
        Map<String, Object> result = new HashMap<>();
        Long targetBrokerId = accountClient.getAccountBrokerId(po.getTransferInAccount());
        if (targetBrokerId == 0) {
            result.put("transferInAccountError", true);
        }

        List<String> errorAccounts = new ArrayList<>();
        List<String> insuffiBalanceAccounts = new ArrayList<>();
        List<AddSaasTransferPO.TransferOutAccount> outAccounts = po.getTransferOutAccounts();
        for (AddSaasTransferPO.TransferOutAccount account : outAccounts) {
            try {
                long accountId = account.getAccountId();
                Long brokerId = accountClient.getAccountBrokerId(accountId);
                if (brokerId == 0 || account.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                    errorAccounts.add(account.getAccountId().toString());
                    continue;
                }

                String tokenId = StringUtils.isEmpty(account.getTokenId()) ? po.getTokenId() : account.getTokenId();
                account.setOrgId(brokerId);
                account.setTokenId(tokenId);
                BalanceDetailDTO balanceDetail = assetClient.getBalance(accountId, tokenId);
                log.info("token:{} account:{} available:{}", tokenId, account.getAccountId(), balanceDetail == null ? "0" : balanceDetail.getAvailable());
                if (balanceDetail == null || balanceDetail.getAvailable().compareTo(account.getAmount()) < 0) {
                    log.warn("balance not enough token:{} account:{} available:{} transfer:{}", tokenId, account.getAccountId(),
                            balanceDetail == null ? "0" : balanceDetail.getAvailable(), account.getAmount());
                    insuffiBalanceAccounts.add(accountId + "");
                }
            } catch (Exception e) {
                log.error("addTransferVerifyInfo ", e);
                throw new BizException(ErrorCode.ERROR);
            }
        }


        if (errorAccounts.size() > 0) {
            result.put("errorTransferOutAccounts", errorAccounts);
        }
        if (insuffiBalanceAccounts.size() > 0) {
            result.put("insufficientBalanceAccounts", insuffiBalanceAccounts);
        }
        if (result.size() == 0 && !po.isMoreBrokers()) {
            Map<Long, List<String>> users = new HashMap<>();
            users.put(targetBrokerId, Lists.newArrayList(po.getTransferInAccount().toString()));
            for (AddSaasTransferPO.TransferOutAccount account : outAccounts) {
                List<String> list = users.getOrDefault(account.getOrgId(), Lists.newArrayList());
                list.add(account.getAccountId() + "");
                users.put(account.getOrgId(), list);
            }
            if (users.size() > 1) {
                result.put("moreBrokers", users);
            }
        }

        if (result.size() > 0) {
            return ResultModel.error(ErrorCode.SASS_TRANSFER_ERROR_ACCOUNT.getCode(), "", result);
        }

        String recordIdKey = adminUser.getId() + "transfer.record.id";
        if (!submit) { //check时返回id，防止重复提交
            String recordId = idGenerator.nextId()+"";
            redisTemplate.opsForValue().set(recordIdKey, recordId, 10, TimeUnit.MINUTES);
            Map<String, String> item = Maps.newHashMap();
            item.put("reqId", recordId);
            return ResultModel.ok(item);
        }
        String recordId = redisTemplate.opsForValue().get(recordIdKey);
        if (!recordId.equals(po.getReqId().toString())) {
            return ResultModel.error("error reqId");
        }

        Map<String, Object> verifyContentMap = new HashMap<>();
        verifyContentMap.put("tokenId", po.getTokenId());
        verifyContentMap.put("transferInAccount", po.getTransferInAccount());
        verifyContentMap.put("moreBrokers", po.isMoreBrokers());
        verifyContentMap.put("tmplModel", StringUtils.isNotEmpty(po.getSequenceId()));
        String verifyContent = JsonUtil.defaultGson().toJson(verifyContentMap);

        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.SAAS_TRANSFER_BIZ_TYPE)
                .setTitle(po.getTitle())
                .setDescription(po.getDescription())
                .setStatus(VerifyBizRecord.INIT_STATUS)
                .setVerifyContent(verifyContent)
                .setId(po.getReqId())
                .build();
        VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        redisTemplate.delete(recordIdKey);

        long bizRecordId = bizRecord.getId();
        String lockKey = String.format(LOCK_KEY, bizRecordId + "");
        boolean lock = RedisLockUtils.tryLock(redisTemplate, lockKey, 300_000);
        try {
            for (AddSaasTransferPO.TransferOutAccount account : outAccounts) {
                SaasTransferRecord transferRecord = new SaasTransferRecord();
                transferRecord.setId(idGenerator.nextId());
                transferRecord.setBizRecordId(bizRecordId);
                transferRecord.setOrgId(account.getOrgId());
                transferRecord.setAccountId(account.getAccountId());
                transferRecord.setAmount(account.getAmount());
                transferRecord.setTokenId(account.getTokenId());
                transferRecord.setTargetOrgId(targetBrokerId);
                transferRecord.setTargetAccountId(po.getTransferInAccount());
                transferRecord.setStatus(SaasTransferRecord.STATUS_INIT);
                transferRecord.setCreatedAt(System.currentTimeMillis());
                transferRecord.setUpdatedAt(System.currentTimeMillis());
                transferRecordMapper.insertSelective(transferRecord);

                LockBalanceRequest lockBalanceRequest = LockBalanceRequest.newBuilder()
                        .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(account.getOrgId()).build())
                        .setAccountId(account.getAccountId())
                        .setTokenId(account.getTokenId())
                        .setLockAmount(account.getAmount().toPlainString())
                        .setClientReqId(transferRecord.getId())
                        //.setLockedToPositionLocked(true)
                        .build();

                LockBalanceReply lockBalanceReply = grpcBalanceClient.lockBalance(lockBalanceRequest);
                if (lockBalanceReply.getCode() == LockBalanceReply.ReplyCode.SUCCESS) {
                    transferRecord.setStatus(SaasTransferRecord.STATUS_LOCK_BALANCE);
                    transferRecord.setLockId(lockBalanceReply.getLockId());
                    transferRecordMapper.updateByPrimaryKeySelective(transferRecord);
                    log.info("lockBalance req:{} res:{}", TextFormat.shortDebugString(lockBalanceRequest), TextFormat.shortDebugString(lockBalanceReply));
                } else {
                    log.error("lockBalance Failed req:{} res:{}", TextFormat.shortDebugString(lockBalanceRequest), TextFormat.shortDebugString(lockBalanceReply));
                    return ResultModel.error("lock balance failed");
                }
            }
            if (bizRecord.getCurrentVerifyLevel() == BizConstant.VERIFY_FINAL_LEVEL) { //无需审核直接走到头
                verifyFlowService.updateRecordStatus(bizRecordId, VerifyBizRecord.PASSED_STATUS);
                RedisLockUtils.releaseLock(redisTemplate, lockKey);
                transferReloaded();
            } else {
                verifyFlowService.updateRecordStatus(bizRecordId, VerifyBizRecord.VERIFING_STATUS);
            }
        } finally {
            RedisLockUtils.releaseLock(redisTemplate, lockKey);
        }
        return ResultModel.ok();
    }

    public SaasTransferRecordDTO queryRecord(long bizRecordId) {
        VerifyFlowRecordDTO verifyFlowRecordDTO = verifyFlowService.queryRecord(bizRecordId);
        SaasTransferRecordDTO result = new SaasTransferRecordDTO();
        BeanUtils.copyProperties(verifyFlowRecordDTO, result);
        Map<String, Object> verifyContentMap = verifyFlowRecordDTO.getVerifyContent();
        result.setTokenId(MapUtils.getString(verifyContentMap, "tokenId"));
        result.setMoreBrokers(MapUtils.getBoolean(verifyContentMap, "moreBrokers"));
        result.setTransferInAccount(MapUtils.getLong(verifyContentMap, "transferInAccount"));
        result.setTmplModel(MapUtils.getBoolean(verifyContentMap, "tmplModel"));
        if (result.isTmplModel()) {
            return result;
        }

        result.setTransferOutAccounts(getTransferRecords(bizRecordId));
        return  result;
    }

    public List<SaasTransferRecordDTO.TransferOutAccount> getTransferRecords(long bizRecordId) {
        Example example = new Example(SaasTransferRecord.class);
        example.createCriteria().andEqualTo("bizRecordId", bizRecordId);
        List<SaasTransferRecord> records = transferRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(records)) {
            return Lists.newArrayList();
        }
        List<SaasTransferRecordDTO.TransferOutAccount> outAccounts = records.stream().map(a -> {
            SaasTransferRecordDTO.TransferOutAccount account = new SaasTransferRecordDTO.TransferOutAccount();
            account.setAccountId(a.getAccountId());
            account.setAmount(a.getAmount());
            account.setTokenId(a.getTokenId());
            account.setOrgId(a.getOrgId());
            account.setStatus(a.getStatus());
            return account;
        }).collect(Collectors.toList());
        return outAccounts;
    }

    public VerifyFlowError verify(long bizRecordId, boolean passed, String reviewComments, AdminUserReply adminUser) {
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(bizRecordId, adminUser.getId(), adminUser.getUsername(), passed, reviewComments);
        log.info("pair : {}", pair);
        //error handler
        if (pair.getLeft() == VerifyFlowError.OK) {
            if (pair.getRight() == VerifyBizRecord.PASSED_STATUS) { //通过的话实行真正的transfer
                verifyPassedTransfer(bizRecordId);
            }
            if (pair.getRight() == VerifyBizRecord.REJECTED_STATUS) { //拒绝的话 则释放资产
                verifyRejectedUnlockBalance(bizRecordId, VerifyBizRecord.REJECTED_END_STATUS);
            }
        }
        return pair.getLeft();
    }



    private void verifyRejectedUnlockBalance(long bizRecordId, int finalStatus) {
        String lockKey = String.format(LOCK_KEY, bizRecordId + "");
        boolean lock = RedisLockUtils.tryLock(redisTemplate, lockKey, 300_000);
        if (!lock) {
            return;
        }
        try {
            List<SaasTransferRecord> records = getLockedRecords(bizRecordId);
            for (SaasTransferRecord record : records) {
                UnlockBalanceRequest request = UnlockBalanceRequest.newBuilder()
                        .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(record.getOrgId()).build())
                        .setAccountId(record.getAccountId())
                        .setTokenId(record.getTokenId())
                        .setOriginClientReqId(record.getId())
                        .setClientReqId(record.getId())
                        .setUnlockAmount(record.getAmount().stripTrailingZeros().toPlainString())
                      //  .setUnlockFromPositionLocked(true)
                        .setUnlockReason("verify rejected")
                        .build();
                UnlockBalanceResponse response = grpcBalanceClient.unLockBalance(request);
                log.info("unLockBalance req:{} res:{}", TextFormat.shortDebugString(request), TextFormat.shortDebugString(response));
                if (response.getCode() == UnlockBalanceResponse.ReplyCode.SUCCESS) {
                    record.setStatus(SaasTransferRecord.STATUS_REJECTED_UNLOCK_BALANCE);
                    transferRecordMapper.updateByPrimaryKeySelective(record);
                }
            }
            if (CollectionUtils.isEmpty(getLockedRecords(bizRecordId))) {
                verifyFlowService.updateRecordStatus(bizRecordId, finalStatus);
            }
        } finally {
            RedisLockUtils.releaseLock(redisTemplate, lockKey);
        }
    }

    public static final String LOCK_KEY = "transfer_record_%s";

    //定时任务 解决没有按时完成的任务，异常或者其它原因中断
    @Scheduled(cron = "8 1/5 * * * ?")
    public void transferReloaded() {
       List<VerifyBizRecord> list = verifyFlowService.queryRecordsByStatus(Lists.newArrayList(VerifyBizRecord.INIT_STATUS,
               VerifyBizRecord.PASSED_STATUS, VerifyBizRecord.REJECTED_STATUS));
       if (CollectionUtils.isEmpty(list)) {
           return;
       }
       list.forEach(r -> {
           if (r.getStatus() == VerifyBizRecord.PASSED_STATUS) {
               verifyPassedTransfer(r.getId());
           }
           if (r.getStatus() == VerifyBizRecord.REJECTED_STATUS) {
               verifyRejectedUnlockBalance(r.getId(), VerifyBizRecord.REJECTED_END_STATUS);
           }
           if (r.getStatus() == VerifyBizRecord.INIT_STATUS) { //冻结失败 解冻结束任务
               verifyRejectedUnlockBalance(r.getId(), VerifyBizRecord.FAILED_END_STATUS);
           }
       });
    }


    private List<SaasTransferRecord> getLockedRecords(long bizRecordId) {
        Example example = new Example(SaasTransferRecord.class);
        example.createCriteria().andEqualTo("bizRecordId", bizRecordId)
                .andIn("status", Arrays.asList(SaasTransferRecord.STATUS_LOCK_BALANCE));
        List<SaasTransferRecord> records = transferRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }

        return records;
    }

    private void verifyPassedTransfer(long bizRecordId) {
        String lockKey = String.format(LOCK_KEY, bizRecordId + "");
        boolean lock = RedisLockUtils.tryLock(redisTemplate, lockKey, 300_000);
        if (!lock) {
            return;
        }
        try {
            List<SaasTransferRecord> records = getLockedRecords(bizRecordId);
            for (SaasTransferRecord record : records) {
                try {
                    SyncTransferRequest transferRequest = SyncTransferRequest.newBuilder()
                            .setClientTransferId(record.getId())
                            .setSourceOrgId(record.getOrgId())

                            .setSourceFlowSubject(BusinessSubject.ADMIN_TRANSFER)
                            .setSourceFlowSecondSubject(0)
                            .setSourceAccountId(record.getAccountId())
                            .setSourceAccountType(AccountType.GENERAL_ACCOUNT)
                            .setTokenId(record.getTokenId())
                            .setAmount(record.getAmount().stripTrailingZeros().toPlainString())
                            .setFromPosition(true)

                            .setTargetOrgId(record.getTargetOrgId())
                            .setTargetAccountType(AccountType.GENERAL_ACCOUNT)
                            .setTargetAccountId(record.getTargetAccountId())
                            .setTargetFlowSubject(BusinessSubject.ADMIN_TRANSFER)
                            .setTargetFlowSecondSubject(0)

                            .setToPosition(false)
                            .build();
                    SyncTransferResponse response = grpcBalanceClient.syncTransfer(transferRequest);
                    log.info("accountTransfer req:{} res:{}", transferRequest, response);
                    boolean suc = response.getCode() == SyncTransferResponse.ResponseCode.SUCCESS;
                    if (suc) {
                        record.setStatus(SaasTransferRecord.STATUS_TRANSFER_OUT_SUCCESS);
                        transferRecordMapper.updateByPrimaryKeySelective(record);
                        log.info("verifyPassedTransfer suc {}", record);
                    }
                } catch (Exception e) { //直接抛出异常，程序会定时再次执行
                    log.info("verifyPassedTransfer error", e);
                }
            }

            if (CollectionUtils.isEmpty(getLockedRecords(bizRecordId))) {
                verifyFlowService.updateRecordStatus(bizRecordId, VerifyBizRecord.PASSED_END_STATUS);
            }
        } finally {
            RedisLockUtils.releaseLock(redisTemplate, lockKey);
        }
    }




}
