package io.bhex.saas.admin.controller.internal;

import com.google.common.collect.Maps;
import io.bhex.base.account.CancelSymbolOrdersReply;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.quote.FairValue;
import io.bhex.base.token.SymbolDetail;
import io.bhex.base.token.TokenCategory;
import io.bhex.base.token.TokenDetail;
import io.bhex.base.token.TokenDetailInfo;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.dao.SymbolApplyRecordMapper;
import io.bhex.saas.admin.dao.TokenApplyMapper;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.grpc.client.impl.OrderClient;
import io.bhex.saas.admin.http.param.MarketRemovePO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.model.SymbolApplyRecord;
import io.bhex.saas.admin.model.TokenApplyRecord;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.impl.AdminTokenApplyService;
import io.bhex.saas.admin.service.impl.SymbolApplyRecordService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/internal")
public class RootController {
    @Resource
    private ExchangeInfoService exchangeInfoService;
    @Resource
    private BrokerSymbolClient brokerSymbolClient;
    @Resource
    private OrderClient orderClient;

    @Autowired
    private BrokerTokenClient brokerTokenClient;

    @Resource
    private SymbolApplyRecordService symbolApplyRecordService;

    @Resource
    private SymbolApplyRecordMapper symbolApplyRecordMapper;

    @Resource
    private AdminTokenApplyService adminTokenApplyService;

    @Resource
    private TokenApplyMapper tokenApplyMapper;

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/delete_markets")
    public ResultModel deleteMarkets(@RequestParam long exchangeId, @RequestParam String symbolId) {
        MarketRemovePO marketRemovePO = MarketRemovePO.builder()
                .exchangeId(exchangeId)
                .symbolId(symbolId)
                .remark("")
                .build();
        ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(exchangeId);
        ExchangeResultRes<Map<String, Long>> result = FeignConfig.getExchangeGatewayClient(instanceDetail.getGatewayUrl()).marketRemove(marketRemovePO);
        log.info("market remove request:{} result: {}", marketRemovePO, result);
        if (result.getStatus() != 200) {
            log.error("market remove request:{} result: {}", marketRemovePO, result);
            throw new BizException(ErrorCode.ERROR, result.getErr());
        }
        return ResultModel.ok();
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/delete_quota_data")
    public ResultModel deleteQuoteData(@RequestParam long exchangeId, @RequestParam String symbolId) {
        brokerSymbolClient.deleteQuoteData(exchangeId, symbolId);
        return ResultModel.ok();
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/cancel_broker_orders")
    public ResultModel cancelBrokerOrdersInternal(@RequestParam long brokerId, @RequestParam String symbolId) {
        CancelSymbolOrdersReply reply = orderClient.cancelBrokerOrderNew(brokerId, symbolId);
        Map<String, Object> result = Maps.newHashMap();
        result.put("openOrdersCount", reply.getOpenOrdersCount());
        result.put("successCount", reply.getSuccessCount());
        return ResultModel.ok(result);
    }

    @AccessAnnotation(internal = true)
    @RequestMapping("/check/symbol_apply")
    public ResultModel<String> checkSymbolApply(@RequestParam String symbolId) {
        //反向生成币对申请记录
        Example symbolExample = new Example(SymbolApplyRecord.class);
        symbolExample.createCriteria().andEqualTo("symbolId", symbolId);
        SymbolApplyRecord applyRecord = symbolApplyRecordMapper.selectOneByExample(symbolExample);
        if (applyRecord == null) {
            //查询bh,补充记录(只支持现货)
            SymbolDetail symbolDetail = brokerSymbolClient.getBhSymbol(symbolId);
            if (symbolDetail == null || symbolDetail.getCategory() != TokenCategory.MAIN_CATEGORY_VALUE) {
                return ResultModel.error("No spot symbol!");
            }
            SymbolApplyRecord symbolApplyRecord = SymbolApplyRecord.builder()
                    .symbolId(symbolDetail.getSymbolId())
                    .quoteTokenId(symbolDetail.getQuoteTokenId())
                    .baseTokenId(symbolDetail.getBaseTokenId())
                    .quotePrecision(DecimalUtil.toBigDecimal(symbolDetail.getQuotePrecision()))
                    .basePrecision(DecimalUtil.toBigDecimal(symbolDetail.getBasePrecision()))
                    .minTradeQuantity(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeQuantity()))
                    .minTradeAmt(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeAmount()))
                    .minPricePrecision(DecimalUtil.toBigDecimal(symbolDetail.getMinPricePrecision()))
                    .mergeDigitDepth(symbolDetail.getDigitMergeList())
                    .brokerId(symbolDetail.getApplyBrokerId())
                    .exchangeId(symbolDetail.getExchangeId())
                    .state(ApplyStateEnum.ACCEPT.getState())
                    .reason("Check symbol")
                    .onlineTime(0L)
                    .build();
            int result = symbolApplyRecordService.saveSymbolRecord(symbolApplyRecord);
            if (result > 0) {
                return ResultModel.ok("Check symbol apply success!");
            } else {
                return ResultModel.error("Check symbol apply error!" + result);
            }
        } else {
            return ResultModel.ok("The symbol apply exit!");
        }
    }

    @AccessAnnotation(internal = true)
    @RequestMapping("/check/token_apply")
    public ResultModel<String> checkTokenApply(@RequestParam String tokenId) {
        //反向生成币种申请记录
        Example tokenExample = new Example(TokenApplyRecord.class);
        tokenExample.createCriteria().andEqualTo("tokenId", tokenId)
                .andEqualTo("exchangeId", 301L);
        TokenApplyRecord applyRecord = tokenApplyMapper.selectOneByExample(tokenExample);
        if (applyRecord == null) {
            //查询bh,补充记录
            TokenDetail tokenDetail = brokerTokenClient.getBhToken(tokenId);
            if (tokenDetail == null) {
                return ResultModel.error("No token!");
            }
            String parentTokenId = tokenDetail.getParentTokenId();
            if (StringUtils.isBlank(parentTokenId)) {
                parentTokenId = tokenId;
            }
            FairValue fairValue = brokerTokenClient.getFairValue(301L, parentTokenId);
            if (fairValue == null) {
                return ResultModel.error("No fairValue!");
            }
            TokenDetailInfo tokenDetailInfo = brokerTokenClient.getTokenDetailInfo(tokenId);
            if (tokenDetailInfo == null) {
                return ResultModel.error("No tokenDetailInfo!");
            }
            TokenApplyRecord tokenApplyRecord = TokenApplyRecord.builder()
                    .exchangeId(301L)
                    .brokerId(9001L)
                    .tokenType(tokenDetail.getTokenType().getNumber())
                    .tokenId(tokenDetail.getTokenId())
                    .tokenName(tokenDetail.getTokenName())
                    .tokenFullName(tokenDetail.getTokenFullName())
                    .fairValue(DecimalUtil.toBigDecimal(fairValue.getUsd()))
                    .iconUrl(tokenDetail.getIcon())
                    .contractAddress("")
                    .introduction(tokenDetailInfo.getTokenDetail())
                    .maxWithdrawingAmt(BigDecimal.ZERO)
                    .minWithdrawingAmt(DecimalUtil.toBigDecimal(tokenDetail.getWithdrawMinQuantity()))
                    .brokerWithdrawingFee(BigDecimal.ZERO)
                    .minDepositingAmt(DecimalUtil.toBigDecimal(tokenDetail.getDepositMinQuantity()))
                    .feeToken(tokenDetailInfo.getFeeToken())
                    .platformFee(DecimalUtil.toBigDecimal(tokenDetailInfo.getPlatformFee()))
                    .needTag(tokenDetail.getAddressNeedTag() ? 1 : 0)
                    .confirmCount(tokenDetailInfo.getChainConfirmCount())
                    .canWithdrawConfirmCount(tokenDetailInfo.getCanWithdrawConfirmCount())
                    .minPrecision(tokenDetail.getMinPrecision())
                    .exploreUrl(tokenDetail.getExploreUrl())
                    .maxQuantitySupplied(tokenDetail.getMaxQuantitySupplied())
                    .currentTurnover(tokenDetail.getCurrentTurnover())
                    .officialWebsiteUrl(tokenDetail.getOfficialWebsiteUrl())
                    .whitePaperUrl(tokenDetail.getWhitePaperUrl())
                    .publishTime(tokenDetail.getPublishTime())
                    .isPrivateToken(tokenDetail.getIsPrivateToken() ? 1 : 0)
                    .isBaas(tokenDetail.getIsBaas() ? 1 : 0)
                    .isAggregate(tokenDetail.getIsAggregate()?1:0)
                    .isTest(tokenDetail.getIsTest() ? 1 : 0)
                    .isMainstream(tokenDetail.getIsMainstream() ? 1 : 0)
                    .extraTag("{}")
                    .extraConfig("{}")
                    .chainName(tokenDetailInfo.getChainName())
                    .parentTokenId(tokenDetailInfo.getParentTokenId())
                    .chainSequence(tokenDetailInfo.getChainSequence())
                    .state(ApplyStateEnum.ACCEPT.getState())
                    .reason("Check token")
                    .build();
            int result = adminTokenApplyService.saveTokenRecord(tokenApplyRecord);
            if (result > 0) {
                return ResultModel.ok("Check token apply success!");
            } else {
                return ResultModel.error("Check token apply error!" + result);
            }
        } else {
            return ResultModel.ok("The token apply exit!");
        }
    }
}
