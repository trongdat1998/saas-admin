package io.bhex.saas.admin.controller;

import com.google.gson.Gson;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.param.ChangeAdminUserPO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.*;
import io.bhex.saas.admin.controller.param.EditBrokerDueTimePO;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.enums.RegisterOptionType;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import io.bhex.bhop.common.bizlog.ExcludeLogAnnotation;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.controller.param.AddBrokerKycConfigPO;
import io.bhex.saas.admin.http.param.BrokerIdPO;
import io.bhex.saas.admin.http.param.DeleteMarketAccountPO;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.http.param.QueryMarketAccountPO;
import io.bhex.saas.admin.http.param.SaveMarketAccountPO;
import io.bhex.saas.admin.http.param.SymbolMarketAccountDetailPO;
import io.bhex.saas.admin.http.param.SymbolMarketAccountPO;
import io.bhex.saas.admin.http.response.AdminResultRes;
import io.bhex.saas.admin.http.response.SymbolMarketAccountRes;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.OrgContractService;
import io.bhex.saas.admin.util.ResultCode;
import lombok.extern.slf4j.Slf4j;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.controller
 * @Author: ming.xu
 * @CreateDate: 08/08/2018 8:53 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping
public class BrokerController extends BaseController {

    @Autowired
    private BrokerService brokerService;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Autowired
    private VerifyFlowService verifyFlowService;

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/broker")
    public ResultModel createBroker(@RequestBody @Validated BrokerDTO brokerDTO) {
        //todo: default instance id
        brokerDTO.setInstanceId(1L);
        Long brokerId = brokerService.createBroker(brokerDTO.toBroker(), brokerDTO.getDueTime());
        if (brokerId > 0) {
//            if (brokerDTO.getExchangeId() > 0) {
//                orgContractService.editOrgContract(brokerDTO.getExchangeId(), brokerId, brokerId, true, true);
//            }
            return ResultModel.ok(brokerDTO);
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @AccessAnnotation(verifyAuth = false)
    @ExcludeLogAnnotation
    @RequestMapping(value = "/api/v1/broker/query_simple_brokers", method = RequestMethod.POST)
    public ResultModel listSimpleBrokers() {
        List<Broker> brokers = brokerService.queryAllBrokers();
        List<Map<String, Object>> result = brokers.stream().map(b -> {
            Map<String, Object> item = new HashMap<>();
            item.put("brokerId", b.getBrokerId());
            item.put("name", b.getName());
            return item;
        }).collect(Collectors.toList());
        return ResultModel.ok(result);
    }


    @RequestMapping(value = "/api/v1/broker/query_broker", method = RequestMethod.POST)
    public ResultModel<PaginationVO<BrokerInfoRes>> listBroker(@RequestBody @Valid QueryBrokerPO po) {
        PaginationVO<BrokerInfoRes> vo = brokerService.queryBroker(po.getCurrent(), po.getPageSize(), po.getBrokerName(), po.getBrokerId());
        return ResultModel.ok(vo);
    }

    @AccessAnnotation(authIds = {301L, 1402L})
    @RequestMapping(value = "/api/v1/broker/query_broker_detail", method = RequestMethod.POST)
    public ResultModel queryBrokerDetail(@RequestBody @Valid BrokerIdPO po) {
        BrokerInfoRes vo = brokerService.queryBrokerDetail(po.getBrokerId());
        return ResultModel.ok(vo);
    }

    @RequestMapping(value = "/api/v1/broker/earnest_address", method = RequestMethod.POST)
    public ResultModel getEarnestAddress(@RequestBody @Validated IdPO po) {
        Map<String, String> result = new HashMap();
        result.put("earnestAddress", brokerService.getEarnestAddress(po.getId()));
        return ResultModel.ok(result);
    }

    @RequestMapping(value = "/api/v1/broker/enable", method = RequestMethod.POST)
    public ResultModel enableBrokerRole(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.enableBroker(po.getId(), Boolean.TRUE);
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/disable", method = RequestMethod.POST)
    public ResultModel disableBrokerRole(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.enableBroker(po.getId(), Boolean.FALSE);
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/forbid_access", method = RequestMethod.POST)
    public ResultModel forbidBrokerAccess(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.forbidAccess(po.getId());
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/cancel_forbid_access", method = RequestMethod.POST)
    public ResultModel cancelForbidBrokerAccess(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.cancelforbidAccess(po.getId());
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/find", method = RequestMethod.POST)
    public ResultModel showBroker(@RequestBody @Validated IdPO po) {
        Broker broker = brokerService.getBrokerById(po.getId());
        BrokerInfoRes res = new BrokerInfoRes();
        BeanUtils.copyProperties(broker, res);
        return ResultModel.ok(res);
    }

    @RequestMapping(value = "/api/v1/broker/edit", method = RequestMethod.POST)
    public ResultModel updateBroker(@RequestBody @Validated EditBrokerPO po) {
        return ResultModel.error("edit failed");
        //boolean isOk = brokerService.updateBroker(po);
        //return ResultModel.ok(isOk);
    }

    @RequestMapping(value = "/api/v1/broker/edit_register_option", method = RequestMethod.POST)
    public ResultModel updateBrokerRegisterOption(@RequestBody @Validated EditBrokerRegisterOptionPO po) {
        boolean result = brokerService.updateBrokerRegisterOption(po.getId(), RegisterOptionType.codeOf(po.getRegisterOption()));
        return result ? ResultModel.ok() : ResultModel.error("edit failed");
    }

//    @RequestMapping(value = "/dns_setting_end", method = RequestMethod.POST)
//    public ResultModel dnsSettingEnd(@RequestBody @Validated BrokerIdPO po) {
//        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getId());
//        if(instance.getStatus() == 1){
//            return ResultModel.ok();
//        }
//        boolean result = brokerService.updateBrokerInstanceStatus(po.getId(),1);
//        if(result == false){
//            return ResultModel.error("edit failed");
//        }
//        return ResultModel.ok();
//    }


    @RequestMapping(value = "/api/v1/broker/send_set_password_email", method = RequestMethod.POST)
    public ResultModel sendSetPasswordEmail(@RequestBody @Validated IdPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getId());
        if (instance.getStatus() == 2) { //已发送过邮件并设置成功
            return ResultModel.ok();
        }

        brokerService.updateBrokerInstanceStatus(po.getId(), 1);

        boolean res = brokerService.sendSetPasswordEmail(po.getId());
        return ResultModel.ok();
    }

    @PostMapping(value = "/api/v1/broker/add_broker_kyc_config")
    public ResultModel addBrokerKycConfig(@RequestBody @Validated AddBrokerKycConfigPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getBrokerId());
        List<AddBrokerKycConfigPO.BrokerKycConfigItem> configs = po.getConfigs();
        for (AddBrokerKycConfigPO.BrokerKycConfigItem config : configs) {
            config.setBrokerId(po.getBrokerId());
            FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).addBrokerKycConfig(config);
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/broker/broker_kyc_config_list", method = RequestMethod.POST)
    public ResultModel getBrokerKycConfigs(@RequestBody @Validated BrokerIdPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getBrokerId());
        IdPO idPO = new IdPO();
        idPO.setId(po.getBrokerId());
        List<AddBrokerKycConfigPO.BrokerKycConfigItem> configs = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl())
                .getBrokerKycConfigs(idPO).getData();

        return ResultModel.ok(configs);
    }

    @RequestMapping(value = "/api/v1/broker/save_symbol_market_account", method = RequestMethod.POST)
    public ResultModel saveSymbolMarketAccount(@RequestBody @Validated SaveMarketAccountPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getOrgId());
        SymbolMarketAccountPO symbolMarketAccount = SymbolMarketAccountPO.builder()
                .orgId(po.getOrgId())
                .symbolMarketAccountList(Arrays.asList(SymbolMarketAccountDetailPO
                        .builder()
                        .accountId(po.getAccountId())
                        .symbolId(po.getSymbolId())
                        .orgId(po.getOrgId())
                        .makerBuyFee(po.getMakerBuyFee())
                        .makerSellFee(po.getMakerSellFee())
                        .takerBuyFee(po.getTakerBuyFee())
                        .takerSellFee(po.getTakerSellFee())
                        .build()))
                .build();
        log.info("saveSymbolMarketAccount info {}", new Gson().toJson(symbolMarketAccount));
        AdminResultRes adminResultRes =
                FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).saveSymbolMarketAccount(symbolMarketAccount);
        if (adminResultRes.getCode().equals(0)) {
            return ResultModel.ok();
        } else if (adminResultRes.getMsg().equalsIgnoreCase("31018")) {
            return ResultModel.error("Account does not exist");
        } else if (adminResultRes.getMsg().equalsIgnoreCase("32114")) {
            return ResultModel.error("Symbol not found");
        } else {
            return ResultModel.error("internal error code：" + adminResultRes.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/query_symbol_market_list", method = RequestMethod.POST)
    public ResultModel querySymbolMarketAccountList(@RequestBody @Validated QueryMarketAccountPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getOrgId());
        List<SymbolMarketAccountRes> symbolMarketAccountRes
                = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).querySymbolMarketAccountList(po).getData();
        return ResultModel.ok(symbolMarketAccountRes);
    }

    @RequestMapping(value = "/api/v1/broker/delete_market_account", method = RequestMethod.POST)
    public ResultModel getBrokerKycConfigs(@RequestBody @Validated DeleteMarketAccountPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getOrgId());
        FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).deleteSymbolMarketAccount(po);
        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/broker/open_otc_depth_share", method = RequestMethod.POST)
    public ResultModel openOtcShare(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.openOtcShare(po.getId());
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/cancel_otc_depth_share", method = RequestMethod.POST)
    public ResultModel cancelOtcShare(@RequestBody @Validated IdPO po) {
        boolean isOk = brokerService.cancelOtcShare(po.getId());
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }

    @RequestMapping(value = "/api/v1/broker/edit_due_time", method = RequestMethod.POST)
    public ResultModel editBrokerDueTime(@RequestBody @Validated EditBrokerDueTimePO po) {
        boolean isOk = brokerService.updateBrokeDueTimer(po.getId(), po.getDueTime());
        return ResultModel.ok(isOk);
    }

    @RequestMapping(value = "/api/v1/broker/add_broker_change_verify", method = RequestMethod.POST)
    public ResultModel addBrokerChangeVerify(@RequestBody @Validated EditBrokerPO po, AdminUserReply adminUser) {
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.CHANGE_BROKER_BIZ_TYPE)
                .setTitle("券商修改")
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
            return verifyBrokerChangeNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }

    private ResultModel verifyBrokerChangeNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.CHANGE_BROKER_BIZ_TYPE) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            EditBrokerPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), EditBrokerPO.class);
            boolean result = brokerService.updateBroker(verifyPO);
            if (result) {
                return ResultModel.ok();
            }
            return ResultModel.error("edit failed");
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/verify_flow/verify_broker_change", method = RequestMethod.POST)
    public ResultModel verifyBrokerChange(@RequestBody @Validated VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return verifyBrokerChangeNoVerifyAdvance(po, adminUser);
    }
}
