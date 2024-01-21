package io.bhex.saas.admin.controller.internal;

import com.google.gson.Gson;

import com.alibaba.fastjson.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.controller.param.BrokerAccountTradeFeePO;
import io.bhex.saas.admin.http.BhHttpClient;
import io.bhex.saas.admin.http.param.DeleteMarketAccountPO;
import io.bhex.saas.admin.http.param.QueryMarketAccountPO;
import io.bhex.saas.admin.http.param.SaveMarketAccountPO;
import io.bhex.saas.admin.http.param.SymbolMarketAccountDetailPO;
import io.bhex.saas.admin.http.param.SymbolMarketAccountPO;
import io.bhex.saas.admin.http.response.AdminResultRes;
import io.bhex.saas.admin.http.response.SymbolMarketAccountRes;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.service.BrokerService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/accountTradeFee")
public class TradeFeeManagerController extends BaseController {

    @Autowired
    private BrokerService brokerService;


    @BussinessLogAnnotation(opContent = "Set TradeFeeAdjust broker:{#po.brokerId} accountIds:{#po.accountIds} takerFeeAdjust:{#po.takerFeeAdjust}")
    @PostMapping(value = "/set_broker_zero_fee")
    public ResultModel setBrokerZeroFee(@RequestBody @Valid BrokerAccountTradeFeePO po) {
        String error = "";
        for (String accountIdStr : po.getAccountIds().split(",")) {
            po.setAccountId(accountIdStr);
            String s = FeignConfig.getBhClient().changeBrokerTradeFee(po);
            if (!s.equalsIgnoreCase("ok")) {
                error = error + accountIdStr + " " + s + ";";
            }
        }
        if (error.equals("")) {
            return ResultModel.ok();
        }
        return ResultModel.error(error);
    }

    @PostMapping(value = "/list_zero_fee")
    public ResultModel listZeroTakerFeeAdjust(@RequestBody Map<String, String> item) {

        BhHttpClient.TradeFeeAdjustPO po = new BhHttpClient.TradeFeeAdjustPO();
        po.setBrokerId(item.containsKey("brokerId") ? Long.parseLong(item.get("brokerId")) : 0);
        po.setLastId(item.containsKey("lastId") ? Long.parseLong(item.get("lastId")) : 0);
        po.setPageSize(item.containsKey("pageSize") ? Integer.parseInt(item.get("pageSize")) : 100);

        String s = FeignConfig.getBhClient().listZeroTakerFeeAdjust(po);

        List<Map<String, Object>> result = JSON.parseObject(s, List.class);
        return ResultModel.ok(result);
    }


    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/save_symbol_market_account", method = RequestMethod.POST)
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
        } else {
            return ResultModel.error("internal error");
        }
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/query_symbol_market_list", method = RequestMethod.POST)
    public ResultModel querySymbolMarketAccountList(@RequestBody @Validated QueryMarketAccountPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getOrgId());
        List<SymbolMarketAccountRes> symbolMarketAccountRes
                = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).querySymbolMarketAccountList(po).getData();
        log.info("symbolMarketAccountRes {}", new Gson().toJson(symbolMarketAccountRes));
        return ResultModel.ok(symbolMarketAccountRes);
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/delete_market_account", method = RequestMethod.POST)
    public ResultModel getBrokerKycConfigs(@RequestBody @Validated DeleteMarketAccountPO po) {
        BrokerInstanceDetail instance = brokerService.getInstanceInfoByBrokerId(po.getOrgId());
        FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).deleteSymbolMarketAccount(po);
        return ResultModel.ok();
    }
}
