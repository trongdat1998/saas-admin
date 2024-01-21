package io.bhex.saas.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import io.bhex.base.account.*;
import io.bhex.base.proto.BaseRequest;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.GetTokenIdsRequest;
import io.bhex.base.token.TokenDetail;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.BaseReqUtil;
import io.bhex.broker.grpc.account.SubAccount;
import io.bhex.broker.grpc.common.AccountTypeEnum;
import io.bhex.saas.admin.controller.dto.DepositDTO;
import io.bhex.saas.admin.controller.dto.QueryUserByAddressDTO;
import io.bhex.saas.admin.controller.param.SaasBatchReceiptPO;
import io.bhex.saas.admin.dao.DepositReceiptApplyRecordMapper;
import io.bhex.saas.admin.grpc.client.impl.BrokerDepositClient;
import io.bhex.saas.admin.grpc.client.impl.BsUserClient;
import io.bhex.saas.admin.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-15 15:53
 */
@Slf4j
@Service
public class DepositService {

    @Resource
    BrokerDepositClient brokerDepositClient;
    @Resource
    BsUserClient bsUserClient;
    @Resource
    DepositReceiptApplyRecordMapper depositReceiptApplyRecordMapper;

    public List<DepositDTO> queryDepositOrders(Long orgId, Long userId, Long fromId, Long lastId, Long orderId, Integer pageSize, String tokenId, Long startTime,
                                               Long endTime, String txId, Long accountId, Integer receiptType) {
        List<DepositReceiptResult> receiptResults = new ArrayList<>();
        if (receiptType == 1) {//入账
            receiptResults = Arrays.asList(DepositReceiptResult.RECEIPT_AVAILABLE, DepositReceiptResult.RECEIPT_LOCKED);
        } else if (receiptType == 2) {
            receiptResults = Arrays.asList(DepositReceiptResult.CANNOT_RECEIPT, DepositReceiptResult.MIN_DEPOSIT, DepositReceiptResult.ACCOUNT_BLACKLIST_DEPOSIT,
                    DepositReceiptResult.PLATFORM_CLOSE_DEPOSIT, DepositReceiptResult.BROKER_CLOSE_DEPOSIT, DepositReceiptResult.BROKER_WAITING_REVIEW);
        }
        List<String> tokenIds = new ArrayList<>();
        if (StringUtils.isNotEmpty(tokenId)) {
            tokenIds.add(tokenId);
        }
        GetDepositsForAdminRequest request = GetDepositsForAdminRequest.newBuilder()
                .setBaseRequest(BaseReqUtil.getBaseRequest(orgId))
                .setOrgId(orgId)
                .setAccountId(Optional.ofNullable(accountId).orElse(0L))
                .addAllTokenId(tokenIds)
                .setFromDepositRecordId(Optional.ofNullable(fromId).orElse(0L))
                .setEndDepositRecordId(Optional.ofNullable(lastId).orElse(0L))
                .setTxId(txId)
                .setStartTime(Optional.ofNullable(startTime).orElse(0L))
                .setEndTime(Optional.ofNullable(endTime).orElse(0L))
                .setLimit(Optional.ofNullable(pageSize).orElse(0))
                .setOrderId(orderId)
                .addAllReceiptResults(receiptResults)
                .build();
        GetDepositsForAdminReply ordersList = brokerDepositClient.queryOrgDepositOrder(request);
        if (ordersList.getDepositRecordsList().size() <= 0) {
            return new ArrayList<>();
        }
        //查询审批中的记录
        List<Long> orderIds = depositReceiptApplyRecordMapper.queryReceiptApplyOrderIdsByOrgId(orgId);
        List<DepositDTO> orderDtos = ordersList.getDepositRecordsList().stream().map(record -> {
            int status =record.getReceiptResultValue();
            //在审批记录表中，表示处于审核中
            if(orderIds.contains(record.getDepositRecordId())){
                status = DepositReceiptResult.SAAS_UNDER_REVIEW_VALUE;
            }
            DepositDTO dto = new DepositDTO();
            dto.setOrgId(orgId);
            dto.setOrderId(record.getDepositRecordId());
            dto.setUserId(Strings.isNullOrEmpty(record.getBrokerUserId()) ? 0L : Long.parseLong(record.getBrokerUserId()));
            dto.setAccountId(record.getAccountId());
            dto.setTokenId(record.getTokenId());
            dto.setTokenName(record.getToken().getTokenName());
            dto.setAddress(record.getAddress());
            dto.setFromAddress(record.getFromAddress());
            dto.setTokenQuantity(DecimalUtil.toBigDecimal(record.getQuantity()).stripTrailingZeros().toPlainString());
            dto.setTxid(record.getTxId());
            dto.setStatusCode(DepositOrderStatus.forNumber(record.getStatusValue()).name());
            dto.setTime(record.getDepositTime());
            dto.setAddressExt(record.getAddressExt());
            dto.setReceiptResult(status);
            dto.setTargetConfirmNum(record.getTargetConfirmNum());
            dto.setConfirmNum(record.getConfirmNum());
            return dto;
        }).collect(Collectors.toList());
        return orderDtos;
    }

    public List<TokenDetail> getTokenListByIds(long brokerId, List<String> tokenIds) {
        GetTokenIdsRequest request = GetTokenIdsRequest.newBuilder()
                .setBaseRequest(BaseReqUtil.getBaseRequest(brokerId))
                .addAllTokenIds(tokenIds)
                .build();
        return brokerDepositClient.queryQuoteTokens(request).getTokenDetailsList();
    }

    /**
     * 入账
     *
     * @param orgId
     * @param accountId
     * @param orderId
     * @return
     */
    public SaasReceiptReply saasReceipt(Long orgId, Long accountId, Long orderId) {
        SaasReceiptRequest request = SaasReceiptRequest.newBuilder()
                .setOrgId(orgId)
                .setAccountId(accountId)
                .setOrderId(orderId)
                .setAllowReceipt(true)
                .build();
        return brokerDepositClient.saasReceipt(request);

    }

    public SaasBatchReceiptPO statisticsReceiptDetail(SaasBatchReceiptPO po, String[] aids, String[] orderIds) {
        StringJoiner aidStr = new StringJoiner(",");
        StringJoiner orderIdStr = new StringJoiner(",");
        Set<String> oldCache = new HashSet<>();
        List<DepositReceiptResult> receiptResults = Arrays.asList(DepositReceiptResult.CANNOT_RECEIPT, DepositReceiptResult.MIN_DEPOSIT, DepositReceiptResult.ACCOUNT_BLACKLIST_DEPOSIT,
                DepositReceiptResult.PLATFORM_CLOSE_DEPOSIT, DepositReceiptResult.BROKER_CLOSE_DEPOSIT, DepositReceiptResult.BROKER_WAITING_REVIEW);
        //获取审核中的入账记录ID
        List<Long> applyOrderIds = depositReceiptApplyRecordMapper.queryReceiptApplyOrderIds(po.getOrgId());
        Map<String, JSONObject> detail = new HashMap<>();
        for (int i = 0; i < aids.length; i++) {
            try {
                Long aid = Long.parseLong(aids[i]);
                Long orderId = Long.parseLong(orderIds[i]);
                if (oldCache.contains(aid + "#" + orderId) || applyOrderIds.contains(orderId)) {
                    continue;
                }
                GetDepositsForAdminRequest request = GetDepositsForAdminRequest.newBuilder()
                        .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(po.orgId).setAccountId(aid).build())
                        .setAccountId(aid)
                        .setOrderId(orderId)
                        .addAllReceiptResults(receiptResults)
                        .build();
                GetDepositsForAdminReply ordersList = brokerDepositClient.queryOrgDepositOrder(request);
                if (!ordersList.getDepositRecordsList().isEmpty() && receiptResults.contains(ordersList.getDepositRecords(0).getReceiptResult())) {
                    aidStr.add(aid + "");
                    orderIdStr.add(orderId + "");
                    DepositRecord record = ordersList.getDepositRecords(0);
                    if (detail.containsKey(record.getTokenId())) {
                        JSONObject obj = detail.get(record.getTokenId());
                        obj.put("amount", obj.getBigDecimal("amount").add(DecimalUtil.toBigDecimal(record.getQuantity())).stripTrailingZeros().toPlainString());
                        obj.put("count", obj.getInt("count") + 1);
                    } else {
                        JSONObject obj = new JSONObject();
                        obj.put("tokenId", record.getTokenId());
                        obj.put("amount", DecimalUtil.toTrimString(record.getQuantity()));
                        obj.put("count", 1);
                        detail.put(record.getTokenId(), obj);
                    }
                }
                oldCache.add(aid + "#" + orderId);
            } catch (Exception e) {
                log.warn("query receiptDetail error aid:{} orderId:{} {}", aids[i], orderIds[i], e.getMessage(), e);
            }
        }
        po.setReceiptDetails(new ArrayList<>(detail.values()));
        po.setAccountIds(aidStr.toString());
        po.setReceiptOrderIds(orderIdStr.toString());
        return po;
    }

    public void saasBatchReceipt(Long orgId, String[] aids, String[] orderIds,String tokenId,Boolean passed) {
        for (int i = 0; i < aids.length; i++) {
            try {
                //审批通过的才调用平台入账接口
                if(passed){
                    saasReceipt(orgId, Long.parseLong(aids[i]), Long.parseLong(orderIds[i]));
                }
            } catch (Exception e) {
                log.warn("saasBatchReceipt error orgId:{}  AID:{}  orderId:{} {}", orgId, aids[i], orderIds[i], e.getMessage(), e);
            }
            //删除审核提交记录
            depositReceiptApplyRecordMapper.deleteReceiptApply(orgId,Long.parseLong(orderIds[i]));
        }
    }

    public QueryUserByAddressDTO queryUserByAddress(Long orgId, String tokenId, String address, String addressTag) {
        ForAddressRequest request = ForAddressRequest.newBuilder()
//                .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(orgId).build())
                .setTokenId(tokenId)
                .setAddress(address)
                .setAddressExt(addressTag)
                .build();
        ForAddressReply reply = brokerDepositClient.forAddress(request);
        if(reply.getAccountId() == 0){
            throw new BizException(ErrorCode.ERROR, "aaddress is not exit ");
        }
        QueryUserByAddressDTO dto = new QueryUserByAddressDTO();
        dto.orgId = reply.getOrgId();
        dto.userId = Strings.isNullOrEmpty(reply.getBrokerUserId()) ? 0L : Long.parseLong(reply.getBrokerUserId());
        dto.accountId = reply.getAccountId();
        dto.tokenId = tokenId;
        return dto;
    }

    /**
     * 充值列表读的是统计库，需要按照平台的接口兼容方式做兼容
     * 参考平台的兼容代码
     */
    private int convertReceiptType(int originReceiptValue) {
        if (originReceiptValue == 0 || originReceiptValue == 1) {
            return originReceiptValue;
        }
        return 2;
    }

    /**
     * 充值列表读的是统计库，需要按照平台的接口兼容方式做兼容
     * 参考平台的兼容代码
     */
    private int convertCannotReceiptReason(int originReceiptValue, int originReason) {
        int reason;
        switch (originReceiptValue) {
            case 2:
                reason = originReason;
                break;
            case 3:
                reason = 4;
                break;
            case 4:
                reason = 3;
                break;
            case 5:
                reason = 2;
                break;
            case 6:
                reason = 1;
                break;
            default:
                reason = 0;
        }
        return reason;
    }

}
