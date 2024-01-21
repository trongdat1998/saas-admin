package io.bhex.saas.admin.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.grpc.admin.AdminUpdateEmailResponse;
import io.bhex.broker.grpc.admin.GetBrokerUserRequest;
import io.bhex.broker.grpc.admin.ReopenUserLoginResponse;
import io.bhex.broker.grpc.admin.UnfreezeUserResponse;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.BrokerUserDTO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.SimpleBrokerUserPO;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.grpc.client.impl.BsUserClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.utils.StringUtils;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping
public class BrokerUserController extends BsBaseController {

    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Autowired
    private BrokerService brokerService;

    @Autowired
    private BsUserClient userClient;

    @RequestMapping(value = {"/api/v1/broker_user/unfreeze_user_login_apply"}, method = RequestMethod.POST)
    public ResultModel<Void> unfreezeUserLoginApply(@RequestBody @Valid SimpleBrokerUserPO po, AdminUserReply adminUser) {
        Combo2<Long, Long> combo2 = getUserIdAndAccountId(po);
        if (combo2 == null) {
            return ResultModel.error("UserId error");
        }
        Broker broker = brokerService.getBrokerById(po.getOrgId());
        po.setBrokerName(broker.getName());
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.REOPEN_USER_LOGIN)
                .setTitle("登录异常解锁")
                .setDescription("")
                .setStatus(VerifyBizRecord.VERIFING_STATUS)
                .setVerifyContent(JSON.toJSONString(po))
                .build();
        VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        if (bizRecord.getCurrentVerifyLevel() == BizConstant.VERIFY_FINAL_LEVEL) { //无需审核直接走到头
            VerifyPO verifyPO = new VerifyPO();
            verifyPO.setId(bizRecord.getId());
            verifyPO.setPassed(true);
            verifyPO.setReviewComments("");
            return unfreezeUserLoginVerifyNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }


    private ResultModel<Void> unfreezeUserLoginVerifyNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.REOPEN_USER_LOGIN) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            SimpleBrokerUserPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), SimpleBrokerUserPO.class);
            UnfreezeUserResponse response = userClient.unfreezeUserLogin(verifyPO.getOrgId(), verifyPO.getUserId());
            return new ResultModel(response.getRet(), response.getMsg(), null);
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.error("reopenUserLoginVerify error");
    }


    @RequestMapping(value = {"/api/v1/verify_flow/verify_unfreeze_user_login"}, method = RequestMethod.POST)
    public ResultModel<Void> unfreezeUserLoginVerify(@RequestBody @Valid VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return unfreezeUserLoginVerifyNoVerifyAdvance(po, adminUser);
    }

    @RequestMapping(value = {"/api/v1/broker_user/update_user_email_apply"}, method = RequestMethod.POST)
    public ResultModel<Void> updateUserEmailApply(@RequestBody @Valid SimpleBrokerUserPO po, AdminUserReply adminUser) {
        if (StringUtils.isBlank(po.getEmail())) {
            return ResultModel.error("Email is not blank!");
        }
        //判断当前userId是否正常
        GetBrokerUserRequest getBrokerUserRequest = GetBrokerUserRequest.newBuilder()
                .setOrgId(po.getOrgId())
                .setUserId(po.getUserId())
                .build();
        BrokerUserDTO brokerUserDTO = userClient.getBrokerUser(getBrokerUserRequest);
        if (brokerUserDTO == null) {
            return ResultModel.error("UserId error");
        }
        //判断当前email是否已经被使用
        getBrokerUserRequest = GetBrokerUserRequest.newBuilder()
                .setOrgId(po.getOrgId())
                .setEmail(po.getEmail())
                .build();
        brokerUserDTO = userClient.getBrokerUser(getBrokerUserRequest);
        if (brokerUserDTO != null) {
            return ResultModel.error("Email is bind!");
        }
        Broker broker = brokerService.getBrokerById(po.getOrgId());
        po.setBrokerName(broker.getName());
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.REBIND_USER_EMAIL)
                .setTitle("重绑用户邮箱")
                .setDescription("")
                .setStatus(VerifyBizRecord.VERIFING_STATUS)
                .setVerifyContent(JSON.toJSONString(po))
                .build();
        VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        if (bizRecord.getCurrentVerifyLevel() == BizConstant.VERIFY_FINAL_LEVEL) { //无需审核直接走到头
            VerifyPO verifyPO = new VerifyPO();
            verifyPO.setId(bizRecord.getId());
            verifyPO.setPassed(true);
            verifyPO.setReviewComments("");
            return updateUserEmailApplyNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }


    private ResultModel<Void> updateUserEmailApplyNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.REBIND_USER_EMAIL) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            SimpleBrokerUserPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), SimpleBrokerUserPO.class);
            AdminUpdateEmailResponse response = userClient.updateUserEmail(verifyPO.getOrgId(), verifyPO.getUserId(), adminUser.getId(), verifyPO.getEmail());
            return new ResultModel(response.getRet(), response.getMsg(), null);
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.error("reopenUserLoginVerify error");
    }


    @RequestMapping(value = {"/api/v1/verify_flow/verify_update_user_email"}, method = RequestMethod.POST)
    public ResultModel<Void> updateUserEmailVerify(@RequestBody @Valid VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return updateUserEmailApplyNoVerifyAdvance(po, adminUser);
    }
}
