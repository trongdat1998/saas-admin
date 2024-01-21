package io.bhex.saas.admin.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.mapper.AdminUserMapper;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.dao.DepositReceiptApplyRecordMapper;
import io.bhex.saas.admin.dao.VerifyBizRecordMapper;
import io.bhex.saas.admin.dao.VerifyFlowConfigMapper;
import io.bhex.saas.admin.dao.VerifyFlowHistoryMapper;
import io.bhex.saas.admin.model.DepositReceiptApplyRecord;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.model.VerifyFlowConfig;
import io.bhex.saas.admin.model.VerifyFlowHistory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Filter;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VerifyFlowService {

    @Autowired
    private VerifyFlowHistoryMapper historyMapper;
    @Autowired
    private VerifyBizRecordMapper recordMapper;
    @Autowired
    private VerifyFlowConfigMapper configMapper;
    @Autowired
    private AdminUserMapper adminUserMapper;
    @Autowired
    private SnowflakeGenerator idGenerator;
    @Autowired
    private DepositReceiptApplyRecordMapper depositReceiptApplyRecordMapper;

    /**
     * @param orgId
     * @param bizType
     * @param statusList
     * @param lastId
     * @param pageSize
     * @param myUserId
     * @return
     */
    public List<VerifyFlowRecordDTO> queryAllRecords(long orgId, int bizType, List<Integer> statusList, long lastId, int pageSize, Long myUserId) {
        Example example = Example.builder(VerifyBizRecord.class)
                .orderByDesc("id")
                .build();
        PageHelper.startPage(0, pageSize);
        Example.Criteria criteria = example.createCriteria()
                .andEqualTo("orgId", orgId);
        if (lastId > 0) {
            criteria.andLessThan("id", lastId);
        }
        if (!CollectionUtils.isEmpty(statusList)) {
            criteria.andIn("status", statusList);
        }
        if (bizType > 0) {
            criteria.andEqualTo("bizType", bizType);
        }
        criteria.andNotIn("status", Lists.newArrayList(VerifyBizRecord.FAILED_STATUS, VerifyBizRecord.FAILED_END_STATUS));

        List<VerifyBizRecord> records = recordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(records)) {
            return Lists.newArrayList();
        }
        List<AdminUser> adminUsers = queryVerifyAdminUsers(orgId);
        Map<String, String> adminUserMap = new HashMap<>();
        adminUsers.forEach(u -> adminUserMap.put(u.getId().toString(), u.getUsername()));
        List<VerifyFlowRecordDTO> list = new ArrayList<>(records.size());
        for (VerifyBizRecord record : records) {
            VerifyFlowRecordDTO dto = convertVerifyFlowRecordDTO(record);
            list.add(dto);
            List<Map<String, String>> verifyUsers = Lists.newArrayList();
            for (String uid : Lists.newArrayList(record.getVerifyUserIds().split(","))) {
                Map<String, String> item = new HashMap<>();
                item.put("adminUserId", uid);
                item.put("adminUserName", adminUserMap.get(uid));
                verifyUsers.add(item);
            }
            dto.setVerifyUsers(verifyUsers);

        }
        return list;
    }

    public List<VerifyFlowRecordDTO> queryVerifiedRecords(long orgId, int bizType, long lastId, int pageSize, Long myUserId) {
        List<VerifyFlowConfig> configs = null;
        if (bizType > 0) {
            VerifyFlowConfig config = configMapper.getVerifyFlowConfig(orgId, bizType);
            if (config != null) {
                configs = Lists.newArrayList(config);
            }
        } else {
            configs = configMapper.getAvailabeVerifyFlowConfigs(orgId);
        }
        if (CollectionUtils.isEmpty(configs)) {
            return Lists.newArrayList();
        }
        configs = configs.stream()
                .filter(c -> {
                    int myLevel = Lists.newArrayList(c.getVerifyUserIds().split(",")).indexOf(myUserId.toString()) + 1;
                    return myLevel > 0;
                })
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(configs)) {
            return Lists.newArrayList();
        }
        for (VerifyFlowConfig config : configs) {
            int myLevel = Lists.newArrayList(config.getVerifyUserIds().split(",")).indexOf(myUserId.toString()) + 1;
            config.setLevel(myLevel);
        }

        List<VerifyBizRecord> records = recordMapper.getVerifiedFlowRecords(orgId, bizType, lastId, configs, pageSize, myUserId);
        if (CollectionUtils.isEmpty(records)) {
            return Lists.newArrayList();
        }
        List<AdminUser> adminUsers = queryVerifyAdminUsers(orgId);
        Map<String, String> adminUserMap = new HashMap<>();
        adminUsers.forEach(u -> adminUserMap.put(u.getId().toString(), u.getUsername()));
        List<VerifyFlowRecordDTO> list = new ArrayList<>(records.size());
        for (VerifyBizRecord record : records) {
            VerifyFlowRecordDTO dto = convertVerifyFlowRecordDTO(record);
            int myLevel = Lists.newArrayList(record.getVerifyUserIds().split(",")).indexOf(myUserId.toString()) + 1;
            dto.setMyVerifyLevel(myLevel);
            List<Map<String, String>> verifyUsers = Lists.newArrayList();
            for (String uid : Lists.newArrayList(record.getVerifyUserIds().split(","))) {
                Map<String, String> item = new HashMap<>();
                item.put("adminUserId", uid);
                item.put("adminUserName", adminUserMap.get(uid));
                verifyUsers.add(item);
            }
            dto.setVerifyUsers(verifyUsers);
            list.add(dto);
        }
        list = list.stream()
                .filter(r -> r.getStatus() != VerifyBizRecord.REJECTED_END_STATUS
                        || (r.getMyVerifyLevel() <= Lists.newArrayList(r.getVerifyHistories()).size())).collect(Collectors.toList()); //拒绝记录只展示给审核过的人
        return list;
    }

    private VerifyFlowRecordDTO convertVerifyFlowRecordDTO(VerifyBizRecord record) {
        if (record == null) {
            return new VerifyFlowRecordDTO();
        }
        VerifyFlowRecordDTO dto = new VerifyFlowRecordDTO();
        BeanUtils.copyProperties(record, dto);
        dto.setVerifyContent(JSON.parseObject(record.getVerifyContent(), Map.class));
        List<VerifyFlowHistory> histories = historyMapper.getHistories(record.getOrgId(), record.getId());
        List<VerifyFlowRecordDTO.VerifyHistory> dtoHistories = new ArrayList<>();
        for (VerifyFlowHistory history : histories) {
            VerifyFlowRecordDTO.VerifyHistory dtoHistory = new VerifyFlowRecordDTO.VerifyHistory();
            BeanUtils.copyProperties(history, dtoHistory);
            dtoHistories.add(dtoHistory);
        }
        dto.setVerifyHistories(dtoHistories);
        return dto;
    }

    public VerifyFlowRecordDTO queryRecord(long bizRecordId) {
        return convertVerifyFlowRecordDTO(recordMapper.selectByPrimaryKey(bizRecordId));
    }

    @Transactional
    public VerifyBizRecord addVerifyBizRecord(AddVerifyRecordRequest request) {
        //单笔充币校验是否已提交审核
        if (request.getBizType() == BizConstant.SAAS_RECEIPT) {
            JSONObject verifyContent = JSONObject.parseObject(request.getVerifyContent());
            DepositReceiptApplyRecord applyRecord = depositReceiptApplyRecordMapper.getReceiptApplyByOrderId(verifyContent.getLong("orgId"), verifyContent.getLong("orderId"));
            if (applyRecord != null) {
                throw new BizException("The order is under review");
            }
        }
        VerifyBizRecord record = new VerifyBizRecord();
        BeanUtils.copyProperties(request, record);

        VerifyFlowConfig config = configMapper.getVerifyFlowConfig(request.getOrgId(), request.getBizType());
        if (config != null) {
            record.setVerifyUserIds(config.getVerifyUserIds());
            record.setCurrentVerifyLevel(1);
        } else {
            record.setVerifyUserIds("");
            record.setCurrentVerifyLevel(BizConstant.VERIFY_FINAL_LEVEL);
        }

        record.setCreatedAt(System.currentTimeMillis());
        record.setUpdatedAt(System.currentTimeMillis());
        if (request.getId() > 0) {
            record.setId(request.getId());
        } else {
            record.setId(idGenerator.nextId());
        }
        recordMapper.insertSelective(record);
        //添加充币审核记录
        if(request.getBizType() == BizConstant.SAAS_RECEIPT){
            JSONObject verifyContent = JSONObject.parseObject(request.getVerifyContent());
            DepositReceiptApplyRecord applyRecord = DepositReceiptApplyRecord.builder()
                    .orgId(verifyContent.getLong("orgId"))
                    .tokenId(verifyContent.getString("tokenId"))
                    .orderId(verifyContent.getLong("orderId"))
                    .created(System.currentTimeMillis())
                    .updated(System.currentTimeMillis())
                    .build();
            depositReceiptApplyRecordMapper.insertSelective(applyRecord);
        }
        //多笔充币审核添加审核申请记录
        if(request.getBizType() == BizConstant.SAAS_BATCH_RECEIPT){
            JSONObject verifyContent = JSONObject.parseObject(request.getVerifyContent());
            String[] orderIds = verifyContent.getString("receiptOrderIds").split(",");
            for (String orderId : orderIds){
                DepositReceiptApplyRecord applyRecord = DepositReceiptApplyRecord.builder()
                        .orgId(verifyContent.getLong("orgId"))
                        .tokenId(verifyContent.getString("tokenId"))
                        .orderId(Long.parseLong(orderId))
                        .created(System.currentTimeMillis())
                        .updated(System.currentTimeMillis())
                        .build();
                depositReceiptApplyRecordMapper.insertSelective(applyRecord);
            }
        }

        return record;
    }

    public int updateRecordStatus(long bizRecordId, int status) {
        return recordMapper.updateRecordStatus(bizRecordId, status);
    }


    @Transactional
    public Pair<VerifyFlowError, Integer> addVerifyResult(long bizRecordId, long adminUserId, String adminUserName,
                                                          boolean passed, String reviewComments) {
        VerifyBizRecord record = recordMapper.selectByPrimaryKey(bizRecordId);
        if (record.getStatus() != VerifyBizRecord.VERIFING_STATUS) {
            return Pair.of(VerifyFlowError.VERIFY_STATUS_ERROR, record.getStatus());
        }

        boolean lastVerify;
        if (record.getCurrentVerifyLevel() != BizConstant.VERIFY_FINAL_LEVEL) {
            List<String> verifyUserIds = Lists.newArrayList(record.getVerifyUserIds().split(","));
            if (!verifyUserIds.contains(adminUserId + "")) {
                return Pair.of(VerifyFlowError.NOT_IN_VERIFY_USER_LIST, record.getStatus());
            }

            List<VerifyFlowHistory> histories = historyMapper.getHistories(record.getOrgId(), record.getId());
            int currentIndex = CollectionUtils.isEmpty(histories) ? 0 : histories.size();
            String currentVerifyUserId = verifyUserIds.get(currentIndex);
            if (!currentVerifyUserId.equals(adminUserId + "")) {
                return Pair.of(VerifyFlowError.NOT_CURRENT_VERIFY_USER, record.getStatus());
            }

            VerifyFlowHistory verifyFlowHistory = new VerifyFlowHistory();
            verifyFlowHistory.setOrgId(record.getOrgId());
            verifyFlowHistory.setBizRecordId(bizRecordId);
            verifyFlowHistory.setVerifyStatus(passed ? 1 : 2);
            verifyFlowHistory.setReviewComments(Strings.nullToEmpty(reviewComments));
            verifyFlowHistory.setAdminUserId(adminUserId);
            verifyFlowHistory.setAdminUserName(adminUserName);
            verifyFlowHistory.setVerifyTime(System.currentTimeMillis());
            verifyFlowHistory.setUpdatedAt(System.currentTimeMillis());
            historyMapper.insertSelective(verifyFlowHistory);
            lastVerify = currentIndex + 1 == verifyUserIds.size();
        } else {
            lastVerify = true;
        }


        if (!passed) {
            record.setStatus(VerifyBizRecord.REJECTED_STATUS);
            record.setCurrentVerifyLevel(BizConstant.VERIFY_FINAL_LEVEL);
        } else {

            if (lastVerify) {
                record.setStatus(VerifyBizRecord.PASSED_STATUS);
                record.setCurrentVerifyLevel(BizConstant.VERIFY_FINAL_LEVEL);
            } else {
                record.setCurrentVerifyLevel(record.getCurrentVerifyLevel() + 1);
            }
        }
        record.setUpdatedAt(System.currentTimeMillis());
        recordMapper.updateByPrimaryKey(record);
        return Pair.of(VerifyFlowError.OK, record.getStatus());
    }


    public List<VerifyBizRecord> queryRecordsByStatus(List<Integer> statusList) {
        Example example = new Example(VerifyBizRecord.class);
        example.createCriteria()
                .andIn("status", statusList);
        List<VerifyBizRecord> records = recordMapper.selectByExample(example);
        return records;
    }

    public List<AdminUser> queryVerifyAdminUsers(long orgId) {
        Example example = new Example(AdminUser.class);
        example.createCriteria().andEqualTo("orgId", orgId)
                .andEqualTo("status", 1);
        List<AdminUser> records = adminUserMapper.selectByExample(example);
        return records;
    }

    public void editVerifyConfig(long orgId, int bizType, boolean canChange, boolean canClose, List<Long> adminUserIds, Long adminUserId, String adminUserName) {
        VerifyFlowConfig config = configMapper.getVerifyFlowConfig(orgId, bizType);
        if (config == null) {
            config = new VerifyFlowConfig();
            config.setOrgId(orgId);
            config.setLevel(adminUserIds.size());
            config.setVerifyUserIds(String.join(",", adminUserIds.stream().map(u -> u + "").collect(Collectors.toList())));
            config.setBizType(bizType);
            config.setCreatedAt(System.currentTimeMillis());
            config.setUpdatedAt(System.currentTimeMillis());
            config.setStatus(1);
            config.setCanChange(canChange ? 1 : 0);
            config.setCanClose(canClose ? 1 : 0);
            config.setAdminUserId(adminUserId);
            config.setAdminUserName(adminUserName);
            configMapper.insertSelective(config);
        } else {
            if (config.getCanChange() == 0) {
                throw new BizException("can't change verify flow");
            }
            config.setLevel(adminUserIds.size());
            config.setVerifyUserIds(String.join(",", adminUserIds.stream().map(u -> u + "").collect(Collectors.toList())));
            config.setAdminUserId(adminUserId);
            config.setAdminUserName(adminUserName);
            configMapper.updateByPrimaryKeySelective(config);
        }
    }

    public void closeVerifyConfig(long orgId, long id) {
        VerifyFlowConfig config = configMapper.selectByPrimaryKey(id);
        if (config.getCanClose() == 0) {
            throw new BizException("can't close verify flow");
        }

        if (config.getOrgId() != orgId) {
            throw new BizException("error parameter!");
        }

        config.setStatus(0);
        config.setUpdatedAt(System.currentTimeMillis());
        configMapper.updateByPrimaryKeySelective(config);
    }

    public List<VerifyFlowConfig> queryVerifyFlowConfigs(long orgId) {
        return configMapper.getVerifyFlowConfigs(orgId);
    }

}
