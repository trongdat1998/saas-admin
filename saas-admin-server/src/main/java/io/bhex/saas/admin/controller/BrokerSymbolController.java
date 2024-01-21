package io.bhex.saas.admin.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import io.bhex.base.account.CancelSymbolOrdersReply;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.token.*;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.bhop.common.util.validation.ValidUtil;
import io.bhex.broker.grpc.admin.AddTokenReply;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.constants.OpTypeConstant;
import io.bhex.saas.admin.controller.dto.BrokerSymbolDTO;
import io.bhex.saas.admin.controller.dto.SymbolRecordDTO;
import io.bhex.saas.admin.controller.param.*;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.impl.OrderClient;
import io.bhex.saas.admin.http.param.MarketRemovePO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.service.BrokerSymbolService;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.impl.MarketAddService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/5 下午12:13
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/broker_symbol")
public class BrokerSymbolController {

    @Autowired
    private BrokerSymbolService brokerSymbolService;
    @Autowired
    private BhOrgClient bhOrgClient;
    @Autowired
    private MarketAddService marketAddService;

    private static final String EX_SYMBOL_HASHKEY = "saas.exchange.symbol";

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @PostMapping("/copy/quote")
    public ResultModel copyExchangeSymbolQuote(@RequestBody @Validated CopyQuotePO copyQuotePO) {
        brokerSymbolService.copySymbolQuote(copyQuotePO.getFromExchangeId(), copyQuotePO.getToExchangeId(),
                copyQuotePO.getSymbolId());
        return ResultModel.ok("请等待两分钟查看效果，不要重复操作");
    }

    @PostMapping("/change/broker")
    public ResultModel changeSymbolBrokerId(@RequestBody @Validated ChangeSymbolBrokerPO changeSymbolExchangePO) {
        brokerSymbolService.changeSymbolBroker(changeSymbolExchangePO.getBrokerId(),
                changeSymbolExchangePO.getSymbolId(),
                changeSymbolExchangePO.getToBrokerId());
        return ResultModel.ok();
    }

    @PostMapping("/deleteQuote")
    public ResultModel deleteQuoteData(@RequestBody @Validated DeleteQuotePO deleteQuotePO) {
        brokerSymbolService.deleteQuoteData(deleteQuotePO.getExchangeId(), deleteQuotePO.getSymbol());
        return ResultModel.ok("请等待两分钟查看效果，不要重复操作");
    }

    @GetMapping("/detail")
    public ResultModel<SymbolRecordDTO> querySymbolDetail(@RequestParam String symbolId) {
        SymbolDetail detail = brokerSymbolService.getSymbolBySymbolId(symbolId);
        return ResultModel.ok(SymbolRecordDTO.parseSymbolDetail(detail));
    }

    @PostMapping("/application/audit")
    public ResultModel auditSymbolApplication(@RequestBody @Validated AuditPO auditPO, AdminUserReply adminUser) {
        ApplyStateEnum curStateEnum = ApplyStateEnum.getByState(auditPO.getCurState());
        ApplyStateEnum toStateEnum = ApplyStateEnum.getByState(auditPO.getToState());

        if (curStateEnum.equals(toStateEnum) && curStateEnum != ApplyStateEnum.ACCEPT) {
            return ResultModel.ok();
        }
        if (StringUtils.isEmpty(auditPO.getSymbolId())) {
            auditPO.setSymbolId(auditPO.getBaseToken().concat(auditPO.getQuoteToken()));
        }
        brokerSymbolService.auditSymbolRecord(adminUser.getOrgId(), auditPO);
        return ResultModel.ok();
    }

    @PostMapping("/application/list")
    public ResultModel<PaginationVO<SymbolRecordDTO>> queryApplicationList(@RequestBody @Valid QuerySymbolApplicationsPO po) {
        if (StringUtils.isNotEmpty(po.getSymbol()) && !ValidUtil.isSymbol(po.getSymbol().toUpperCase())) {
            return ResultModel.ok();
        }
        ApplyStateEnum stateEnum = ApplyStateEnum.getByState(po.getState());
        return ResultModel.ok(brokerSymbolService.applicationList(po.getBrokerId(), po.getCurrent(), po.getPageSize(),
                stateEnum.getState(), po.getSymbol()));
    }

    @RequestMapping(value = "/query")
    public ResultModel<BrokerSymbolDTO> querySymbols(@RequestBody QueryBrokerSymbolsPO po) {
        Long brokerId = po.getBrokerId();
        if (StringUtils.isNotEmpty(po.getSymbol()) && !ValidUtil.isSymbol(po.getSymbol().toUpperCase())) {
            return ResultModel.ok();
        }
        PaginationVO<BrokerSymbolDTO> vo = brokerSymbolService.query(brokerId, po.getCurrent(),
                po.getPageSize(), po.getCategory(), po.getOwner(),
                Strings.nullToEmpty(po.getSymbol()).toUpperCase().replaceAll("/", ""));

        return ResultModel.ok(vo);
    }

    @RequestMapping(value = "/batch_allow_show", method = RequestMethod.POST)
    public ResultModel symbolBatchAllowShow(@RequestBody @Valid BatchAllowSymbolPO po) {
        List<String> symbols = po.getSymbols()
                .stream().map(s -> s.replace("/", "").toUpperCase())
                .collect(Collectors.toList());

        for (String symbolId : symbols) {
            for (Long brokerId : po.getBrokers()) {
                SymbolDetail symbolDetail = brokerSymbolService.getSymbolBySymbolId(symbolId);
                if (symbolDetail == null) {
                    return ResultModel.error(symbolId + " not existed.");
                }
                if (symbolDetail.getIsPrivate() && symbolDetail.getApplyBrokerId() != brokerId) {
                    ExchangeReply sourceExchange = bhOrgClient.findExchangeByBrokerId(symbolDetail.getApplyBrokerId());
                    ExchangeReply targetExchange = bhOrgClient.findExchangeByBrokerId(brokerId);
                    if (sourceExchange.getExchangeId() != targetExchange.getExchangeId()) {
                        log.warn("private symbol {} {} not the same exchange", symbolDetail.getApplyBrokerId(), brokerId);
                        return ResultModel.error(symbolId + " is private token, owner:" + symbolDetail.getApplyBrokerId());
                    }
                }
            }
        }
        brokerSymbolService.batchPublishBrokerSymbol(po);
        return ResultModel.ok();
    }

    @RequestMapping(value = "/batch_forbid_show", method = RequestMethod.POST)
    public ResultModel symbolBatchForbidShow(@RequestBody @Valid BatchAllowSymbolPO po) {
        brokerSymbolService.batchCloseBrokerSymbol(po);
        return ResultModel.ok();
    }

//    @BussinessLogAnnotation(name = OpTypeConstant.SYMBOL_ALLOW_SHOW,
//            entityId = "{#po.exchangeId.toString().concat('-').concat(#po.symbolId)}")
//    @RequestMapping(value = "/allow_show", method = RequestMethod.POST)
//    public ResultModel allowShow(@RequestBody @Valid EditExchangeSymbolPO po) {
//        po.setSymbolId(po.getSymbolId().toUpperCase());
//        SymbolDetail symbolDetail = brokerSymbolService.getSymbolBySymbolId(po.getSymbolId());
//        if (symbolDetail.getCategory() == TokenCategory.FUTURE_CATEGORY_VALUE) { //合约只是简单的修改状态
//            boolean r = brokerSymbolService.updateShowStatusInExchange(po.getExchangeId(), po.getSymbolId(), true);
//            log.info("allow show category:{} res:{}", symbolDetail.getCategory(), r);
//            return ResultModel.ok();
//        }
//
//        if (symbolDetail.getCategory() == TokenCategory.MAIN_CATEGORY_VALUE) {
//            TokenDetailInfo tokenDetailInfo = exchangeTokenService.getBhTokenInfo(symbolDetail.getBaseTokenId());
//            if (tokenDetailInfo.getPrivateTokenExchangeId() > 0
//                    && tokenDetailInfo.getPrivateTokenExchangeId() != po.getExchangeId()) {
//                return ResultModel.error(symbolDetail.getBaseTokenId() + " is private token, owner:" + tokenDetailInfo.getPrivateTokenExchangeId());
//            }
//
//            tokenDetailInfo = exchangeTokenService.getBhTokenInfo(symbolDetail.getQuoteTokenId());
//            if (tokenDetailInfo.getPrivateTokenExchangeId() > 0
//                    && tokenDetailInfo.getPrivateTokenExchangeId() != po.getExchangeId()) {
//                return ResultModel.error(symbolDetail.getBaseTokenId() + " is private token, owner:" + tokenDetailInfo.getPrivateTokenExchangeId());
//            }
//        }
//
//        boolean r = brokerSymbolService.updateShowStatusInExchange(po.getExchangeId(), po.getSymbolId(), true);
//        Combo2<String, String> combo2 = SymbolUtil.partitionName(symbolDetail.getQuoteTokenId(), symbolDetail.getCategory(), po.getExchangeId());
//
//        boolean r2 = brokerSymbolService.publishExchangeSymbol(po.getExchangeId(), po.getSymbolId(), combo2.getV1());
//        if (!r2) {
//            return ResultModel.error("publishExchangeSymbol error");
//        }
//        AllowExchangeSymbolTradeReply reply = brokerSymbolService.exchangeAllowTrade(po.getExchangeId(), po.getSymbolId(), YesNoEnum.YES);
//        if (!reply.getResult()) {
//            return ResultModel.error("exchangeAllowTrade error");
//        }
//
//        try {
//            List<Integer> dumpScales = SymbolUtil.convertDigitalMergedListToDumpScales(symbolDetail.getDigitMergeList());
//            Boolean reverse = Objects.nonNull(symbolDetail) ? symbolDetail.getIsReverse() : false;
//            MarketAddPO marketAddPO = MarketAddPO.builder()
//                    .partitionName(combo2.getV1())
//                    .topicName(combo2.getV2())
//                    .exchangeId(po.getExchangeId())
//                    .symbolId(po.getSymbolId())
//                    .symbolName(po.getSymbolId())
//                    .remark("new symbol")
//                    .dumpScales(dumpScales)
//                    .isReverse(reverse)
//                    .firstReqTime(System.currentTimeMillis())
//                    .build();
//
//            marketAddService.marketAdd(marketAddPO);
//        } catch (Exception e) {
//            log.error("call exchange http error. req:{}", po, e);
//            return ResultModel.error("call exchange http error");
//        }
//
//        return ResultModel.ok();
//    }

    @BussinessLogAnnotation(name = OpTypeConstant.SYMBOL_FORBID_SHOW,
            entityId = "{#po.exchangeId.toString().concat('-').concat(#po.symbolId)}")
    @RequestMapping(value = "/forbid_show", method = RequestMethod.POST)
    public ResultModel forbidShow(@RequestBody @Valid EditBrokerSymbolPO po) {
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(po.getBrokerId());
        brokerSymbolService.updateShowStatusInExchange(exchangeReply.getExchangeId(), po.getSymbolId(), false);

        return ResultModel.ok();
    }

    @BussinessLogAnnotation(name = OpTypeConstant.SYMBOL_ENABLE)
    @RequestMapping(value = "/enable", method = RequestMethod.POST)
    public ResultModel allowPublish(@RequestBody @Valid EditBrokerSymbolPO po) {
        //exchangeSymbolService.enableSymbol(po.getExchangeId(), po.getSymbolId(), true);
        return ResultModel.ok();
    }

    @BussinessLogAnnotation(name = OpTypeConstant.SYMBOL_DISABLE)
    @RequestMapping(value = "/disable", method = RequestMethod.POST)
    public ResultModel forbidPublish(@RequestBody @Valid EditBrokerSymbolPO po) {
        //exchangeSymbolService.enableSymbol(po.getExchangeId(), po.getSymbolId(), false);

        return ResultModel.ok();
    }

    @BussinessLogAnnotation(opContent = "allow trade for {#po.brokerId} {#po.symbolId}")
    @RequestMapping(value = "/allow_trade", method = RequestMethod.POST)
    public ResultModel saasAllowTrade(@RequestBody @Valid EditBrokerSymbolPO po) {
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(po.getBrokerId());
        boolean r = brokerSymbolService.saasAllowTrade(exchangeReply.getExchangeId(), po.getSymbolId(), true);

        return ResultModel.ok(r);
    }

    @BussinessLogAnnotation(opContent = "forbid trade for {#po.brokerId} {#po.symbolId}")
    @RequestMapping(value = "/forbid_trade", method = RequestMethod.POST)
    public ResultModel saasForbidTrade(@RequestBody @Valid EditBrokerSymbolPO po) {
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(po.getBrokerId());
        boolean r = brokerSymbolService.saasAllowTrade(exchangeReply.getExchangeId(), po.getSymbolId(), false);
        return ResultModel.ok(r);
    }



}
