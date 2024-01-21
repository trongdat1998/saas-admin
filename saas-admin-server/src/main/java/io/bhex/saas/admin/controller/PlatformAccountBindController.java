package io.bhex.saas.admin.controller;

import io.bhex.base.account.AccountType;
import io.bhex.base.account.BindAccountReply;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.config.OrgInstanceConfig;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.param.BalanceDetailDTO;
import io.bhex.bhop.common.dto.param.BrokerInstanceRes;
import io.bhex.bhop.common.dto.param.PlatformAccountAssetsPO;
import io.bhex.bhop.common.dto.param.PlatformAccountBindAccountPO;
import io.bhex.bhop.common.grpc.client.AccountAssetClient;
import io.bhex.bhop.common.grpc.client.BhAccountClient;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.constants.OpTypeConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/platform_account")
public class PlatformAccountBindController extends BaseController {



    @Autowired
    private OrgInstanceConfig orgInstanceConfig;
    @Autowired
    private BhAccountClient bhAccountClient;
    @Autowired
    private AccountAssetClient accountAssetClient;


    private boolean checkAccountType(int accountType) {
        if (accountType != AccountType.OPERATION_ACCOUNT_VALUE
                && accountType != AccountType.SAAS_REVENUE_ACCOUNT_VALUE) {
            return false;
        }
        return true;
    }



    @RequestMapping(value = "/assets", method = RequestMethod.POST)
    public ResultModel<Map<String, Object>> getAssets(@RequestBody @Valid PlatformAccountAssetsPO po) {
        if (!checkAccountType(po.getAccountType())) {
            return ResultModel.error("request.parameter.error");
        }
        Map<String, Object> result = new HashMap<>();
        Long accountId = bhAccountClient.bindRelation(BizConstant.DEFAUT_SAAS_ORG_ID, AccountType.forNumber(po.getAccountType()));
        log.info("accountType:{} accountId:{}", po.getAccountType(), accountId);
        if (accountId == null || accountId == 0) {
            result.put("isBind", false);
            return ResultModel.ok(result);
        }

        Long brokerId = bhAccountClient.getAccountBrokerId(accountId);
        result.put("accountId", String.valueOf(accountId));
        result.put("brokerName", orgInstanceConfig.getBrokerInstance(brokerId).getBrokerName());
        List<BalanceDetailDTO> balanceDetais = accountAssetClient.getBalances(brokerId, accountId);
        if (CollectionUtils.isEmpty(balanceDetais)) {
            return ResultModel.ok(new ArrayList<>());
        }



        result.put("isBind", true);
        result.put("list", balanceDetais);
        return ResultModel.ok(result);
    }




    @BussinessLogAnnotation(name = OpTypeConstant.BIND_ACCOUNT)
    @RequestMapping(value = "/bind", method = RequestMethod.POST)
    public ResultModel bindAccount(@RequestBody @Valid PlatformAccountBindAccountPO po) {
        if (!checkAccountType(po.getAccountType())) {
            return ResultModel.error("request.parameter.error");
        }

        Optional<BrokerInstanceRes> optional = orgInstanceConfig.listBrokerInstances().stream()
                .filter(b -> b.getBrokerName().equals(po.getBrokerName()))
                .findFirst();
        if (!optional.isPresent()) {
            return ResultModel.error("platform_account.broker.name.not.found");
        }

        BindAccountReply reply = bhAccountClient.bindAccount(BizConstant.DEFAUT_SAAS_ORG_ID, po.getAccountId(),
                AccountType.forNumber(po.getAccountType()));
        BindAccountReply.Result r = reply.getResult();
        log.info("bindAccount:{} result:{}", po, r);
        if (r.equals(BindAccountReply.Result.OK)) {
            return ResultModel.ok();
        }
        if (r.equals(BindAccountReply.Result.BINDING)) {
            return ResultModel.error("platform_account.binding");
        }
        if (r.equals(BindAccountReply.Result.ACCOUT_ID_ERROR)) {
            return ResultModel.error("platform_account.accountid.error");
        }
        return ResultModel.error("platform.bind.account.failed");
    }
}
