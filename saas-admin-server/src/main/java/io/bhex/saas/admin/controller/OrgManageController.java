package io.bhex.saas.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.bhex.base.account.GetBrokerExchangeContractReply;
import io.bhex.base.account.GetBrokerExchangeContractRequest;
import io.bhex.base.admin.*;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.param.ChangeAdminUserPO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.EditExchangePO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.ChangeContractPO;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.http.response.AdminResultRes;
import io.bhex.saas.admin.model.*;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.OrgContractService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping
public class OrgManageController  extends BaseController {

    @Autowired
    private BrokerService brokerService;
    @Autowired
    private ExchangeInfoService exchangeInfoService;
    @Autowired
    private OrgContractService orgContractService;
    @Autowired
    private BhOrgClient bhOrgClient;
    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private AdminLoginUserService adminLoginUserService;

    @RequestMapping(value = "/api/v1/{type}/change_admin_user_apply", method = RequestMethod.POST)
    public ResultModel addExchangeChangeVerify(@RequestBody @Validated ChangeAdminUserPO po, @PathVariable String type, AdminUserReply adminUser) {
        po.setType(type);
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.CHANGE_ADMIN_USER)
                .setTitle("AdminUser修改")
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
            return verifyAdminUserChangeNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }

    private ResultModel verifyAdminUserChangeNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.CHANGE_ADMIN_USER) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            ChangeAdminUserPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), ChangeAdminUserPO.class);
            long orgId = verifyPO.getOrgId();
            if (verifyPO.getType().equals("broker")) {
                BrokerInstanceDetail instanceDetail = brokerService.getInstanceInfoByBrokerId(orgId);
                AdminResultRes<Object> result = FeignConfig.getBrokerClient(instanceDetail.getAdminInternalApiUrl()).changeAdminUser(verifyPO);
                if (result.getCode() == 0) {
                    return ResultModel.ok();
                }
                log.warn("res:{}", result);
                return ResultModel.error(result.getMsg());
            } else if (verifyPO.getType().equals("exchange")) {
//                ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(orgId);
//                AdminResultRes<Object> result = FeignConfig.getExchangeAdminClient(instanceDetail.getAdminInternalApiUrl()).changeAdminUser(verifyPO);
//                if (result.getCode() == 0) {
//                    return ResultModel.ok();
//                }
//                log.warn("res:{}", result);
//                return ResultModel.error(result.getMsg());
            }
            return ResultModel.error("edit failed");
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.error("email.error");
    }

    @RequestMapping(value = "/api/v1/verify_flow/verify_admin_user_change", method = RequestMethod.POST)
    public ResultModel verifyAdminUserChange(@RequestBody @Validated VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return verifyAdminUserChangeNoVerifyAdvance(po, adminUser);
    }


    @BussinessLogAnnotation(entityId = "{#po.orgId}")
    @RequestMapping(value = "/api/v1/{type}/change_admin_user", method = RequestMethod.POST)
    public ResultModel changeAdminUser(@RequestBody @Valid ChangeAdminUserPO po, @PathVariable String type) {
        long orgId = po.getOrgId();
        if (type.equals("broker")) {
            BrokerInstanceDetail instanceDetail = brokerService.getInstanceInfoByBrokerId(orgId);
            AdminResultRes<Object> result = FeignConfig.getBrokerClient(instanceDetail.getAdminInternalApiUrl()).changeAdminUser(po);
            if (result.getCode() == 0) {
                return ResultModel.ok();
            }
            log.warn("res:{}", result);
        } else if (type.equals("exchange")) {
//            ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(orgId);
//            AdminResultRes<Object> result = FeignConfig.getExchangeAdminClient(instanceDetail.getAdminInternalApiUrl()).changeAdminUser(po);
//            if (result.getCode() == 0) {
//                return ResultModel.ok();
//            }
//            log.warn("res:{}", result);
        }

        return ResultModel.error("email.error");
    }

    @BussinessLogAnnotation(opContent = "editOrgContract brokerId:{#po.brokerId} exchangeId:{#po.exchangeId} {#po.bind ? 'bind' :'unbind'} ")
    @RequestMapping(value = "/api/v1/{type}/edit_org_contract", method = RequestMethod.POST)
    public ResultModel editOrgContract(@RequestBody @Valid ChangeContractPO po, @PathVariable String type) {
        long applyOrgId = type.equals("broker") ? po.getBrokerId() : po.getExchangeId();

        orgContractService.editOrgContract(po.getExchangeId(), po.getBrokerId(), applyOrgId, po.getBind(), po.getTrust());

        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/{type}/list_{type2}_contract", method = RequestMethod.POST)
    public ResultModel listOrgContract(@RequestBody @Valid ChangeContractPO po, @PathVariable String type, @PathVariable String type2) {
        List<Map<String, Object>> items = new ArrayList<>();
        //type=broker 代表 broker查询合作的交易所
        OrgType orgType = type.equals("broker") ? OrgType.Broker_Org  : OrgType.Exchange_Org;
        long orgId = type.equals("broker") ? po.getBrokerId() : po.getExchangeId();
        ListContractReply reply = orgContractService.listOrgContract(orgId, orgType, 1, 1000);
        List<ContractDetail> contractDetails = reply.getContractDetailList();
        if (CollectionUtils.isEmpty(contractDetails)) {
            return ResultModel.ok(items);
        }
        contractDetails = contractDetails.stream()
                .filter(c -> c.getStatus() == OrgContract.ALLOW_STATUS)
                .collect(Collectors.toList());

        for (ContractDetail detail : contractDetails) {
            Map<String, Object> item = new HashMap<>();
            item.put("brokerId", detail.getBrokerId());
            item.put("exchangeId", detail.getExchangeId());
            if (type.equals("exchange")) {
                Broker broker = brokerService.getBrokerByBrokerId(detail.getBrokerId());
                item.put("apiDomain", broker.getApiDomain());
                BrokerInstanceDetail instanceDetail = brokerService.getInstanceInfoByBrokerId(detail.getBrokerId());
                item.put("adminWebUrl", instanceDetail.getAdminWebUrl());
                item.put("name", broker.getName());
                item.put("created", broker.getCreatedAt().getTime());
                item.put("forbidAccessStatus", instanceDetail.getForbidAccess() == 1);
            } else {
                ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(detail.getExchangeId());
                item.put("adminWebUrl", instanceDetail.getAdminWebUrl());
                item.put("name", instanceDetail.getExchangeName());
                item.put("created", instanceDetail.getCreatedAt().getTime());
                item.put("forbidAccessStatus", instanceDetail.getForbidAccess() == 1);
            }
            GetBrokerExchangeContractReply contractReply = bhOrgClient.getBrokerExchangeContract(GetBrokerExchangeContractRequest.newBuilder()
                    .setBrokerId(detail.getBrokerId()).setExchangeId(detail.getExchangeId())
                    .build());
            item.put("trust", contractReply.getIsTrust());
            items.add(item);
        }

        return ResultModel.ok(items);
    }

}
