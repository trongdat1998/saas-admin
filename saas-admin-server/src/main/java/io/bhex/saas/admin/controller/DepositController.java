package io.bhex.saas.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.DepositDTO;
import io.bhex.saas.admin.controller.dto.DepositOrderListPO;
import io.bhex.saas.admin.controller.dto.QueryUserByAddressDTO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.*;
import io.bhex.saas.admin.dao.DepositReceiptApplyRecordMapper;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.impl.DepositService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-15 15:07
 */
@Slf4j
@RestController
@RequestMapping
public class DepositController extends BsBaseController {
    @Autowired
    DepositService depositService;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Resource
    TaskExecutor taskExecutor;
    @Resource
    DepositReceiptApplyRecordMapper depositReceiptApplyRecordMapper;

    /**
     * 查询借币记录
     *
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/deposit/query_orders", method = RequestMethod.POST)
    public ResultModel queryDepositOrder(@RequestBody @Valid DepositOrderListPO po) {
        Long accountId = 0L;
        if (po.getUserId() != 0) {
            Combo2<Long, Long> combo2 = getUserIdAndAccountId(po);
            if (combo2 == null) {
                return ResultModel.error("UserId error");
            }
            accountId = combo2.getV2();
        }

        List<DepositDTO> list = depositService.queryDepositOrders(po.getOrgId(), po.getUserId(), po.getFromId(), po.getLastId(), po.getOrderId(),
                po.getPageSize(), po.getTokenId().toUpperCase(), po.getStartTime(), po.getEndTime(), po.getTxId(), accountId, po.getReceiptType());

        return ResultModel.ok(list);
    }

    /**
     * 单笔入账申请
     *
     * @param po
     * @param adminUser
     * @return
     */
    @RequestMapping(value = "/api/v1/deposit/saas_receipt_apply", method = RequestMethod.POST)
    public ResultModel addSaasReceiptApply(@RequestBody @Valid SaasReceiptPO po, AdminUserReply adminUser) {

        Broker broker = brokerService.getBrokerById(po.getOrgId());
        po.setBrokerName(broker.getName());
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.SAAS_RECEIPT)
                .setTitle("单笔入账审核")
                .setDescription("")
                .setStatus(VerifyBizRecord.VERIFING_STATUS)
                .setVerifyContent(JSON.toJSONString(po))
                .build();
        VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        return ResultModel.ok();
    }

    /**
     * 多笔入账
     *
     * @param po
     * @param adminUser
     * @return
     */
    @RequestMapping(value = "/api/v1/deposit/saas_batch_receipt_apply", method = RequestMethod.POST)
    public ResultModel addSaasBatchReceiptApply(@RequestBody @Valid SaasBatchReceiptPO po, AdminUserReply adminUser) {

        String[] aids = po.accountIds.split(",");
        String[] orderIds = po.getReceiptOrderIds().split(",");
        if (aids.length != orderIds.length) {
            return ResultModel.error("The aids does not match the orderIds of numbers");
        }
        CompletableFuture.runAsync(() -> {
            Broker broker = brokerService.getBrokerById(po.getOrgId());
            po.setBrokerName(broker.getName());
            depositService.statisticsReceiptDetail(po, aids, orderIds);
            //批量入账的订单都在审批中，直接结束，不添加审核请求
            if(StringUtils.isBlank(po.getReceiptOrderIds())){
                return;
            }
            AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                    .setOrgId(adminUser.getOrgId())
                    .setAdminUserId(adminUser.getId())
                    .setAdminUserName(adminUser.getUsername())
                    .setBizType(BizConstant.SAAS_BATCH_RECEIPT)
                    .setTitle("多笔入账审核")
                    .setDescription("")
                    .setStatus(VerifyBizRecord.VERIFING_STATUS)
                    .setVerifyContent(JSON.toJSONString(po))
                    .build();
            VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        }, taskExecutor);

        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/deposit/query_user", method = RequestMethod.POST)
    public ResultModel queryUserByAddress(@RequestBody @Valid QueryUserByAddressPO po) {
        QueryUserByAddressDTO result = depositService.queryUserByAddress(po.orgId, po.tokenId.toUpperCase(), po.address, po.addressTag);

        return ResultModel.ok(result);
    }

    /**
     * 单笔入账审核
     *
     * @param po
     * @param adminUser
     * @return
     */
    @RequestMapping(value = {"/api/v1/verify_flow/verify_saas_receipt"}, method = RequestMethod.POST)
    public ResultModel verifySaasReceipt(@RequestBody @Valid VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.SAAS_RECEIPT) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        SaasReceiptPO receiptPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), SaasReceiptPO.class);
        //校验通过删除审批记录
        if (pair.getLeft() == VerifyFlowError.OK) {
            //删除审核提交记录
            depositReceiptApplyRecordMapper.deleteReceiptApply(receiptPO.getOrgId() , receiptPO.getOrderId());
        }
        //发起入账
        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            depositService.saasReceipt(receiptPO.getOrgId(), receiptPO.getAccountId(), receiptPO.getOrderId());
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = {"/api/v1/verify_flow/verify_batch_saas_receipt"}, method = RequestMethod.POST)
    public ResultModel verifyBatchSaasReceipt(@RequestBody @Valid VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.SAAS_BATCH_RECEIPT) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());
        //校验通过
        if (pair.getLeft() == VerifyFlowError.OK) {
            SaasBatchReceiptPO receiptPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), SaasBatchReceiptPO.class);
            String[] aids = receiptPO.getAccountIds().split(",");
            String[] orderIds = receiptPO.getReceiptOrderIds().split(",");
            CompletableFuture.runAsync(() -> {
                depositService.saasBatchReceipt(receiptPO.orgId, aids, orderIds, receiptPO.getTokenId(), po.getPassed());
            }, taskExecutor);
            return ResultModel.ok();
        }
        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.ok();
    }


}
