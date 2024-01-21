package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.reflect.TypeToken;
import io.bhex.base.bhadmin.ContractApplyObj;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.percent.PercentageOutputSerialize;
import io.bhex.broker.common.util.JsonUtil;
import io.bhex.saas.admin.util.SymbolUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 2019/10/10 5:23 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolFuturesRecordDTO {

    private Long id;

    private String symbolId;

    private String symbolName;

    private String baseTokenId;

    private String quoteTokenId;

    private String underlyingId; //标的id

    private String displayUnderlyingId; //展示标的id

    private List<SymbolNameLocale> symbolNameLocaleList;

    private List<RiskLimit> riskLimitList;

    private Long exchangeId; //申请的交易所id

    private Long brokerId;

    private String exchangeName; //申请的交易所名称

    private String brokerName;

    private Integer state; //期货审核状态值 0 申请中 1 通过 2 拒绝

    private BigDecimal minTradeQuantity; //单次交易最小交易base的数量

    private BigDecimal minTradeAmount; //最小交易额

    private BigDecimal minPricePrecision; //每次价格变动，最小的变动单位

    private String digitMergeList; //深度合并。格式：0.01,0.0001,0.000001

    private BigDecimal basePrecision;

    private BigDecimal quotePrecision;

    private String displayToken; //显示用的估价token

    private String currency; //计价单位(token_id)

    private String currencyDisplay; //显示价格单位

    private BigDecimal contractMultiplier; //合约乘数

    private BigDecimal limitDownInTradingHours; //交易时段内下跌限价

    private BigDecimal limitUpInTradingHours; //交易时段内上涨限价

    private BigDecimal limitDownOutTradingHours; //交易时段外下跌限价

    private BigDecimal limitUpOutTradingHours; //交易时段外上涨限价

    private BigDecimal maxLeverage; //最大杠杆

    private String leverageRange; //杠杆范围

    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal overPriceUpRange; //超价浮动范围

    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal overPriceDownRange; //超价浮动范围

    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal marketPriceUpRange; //市价浮动范围

    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal marketPriceDownRange; //市价浮动范围

    private Integer isPerpetualSwap;

    private String indexToken; //指数名称

    private String displayIndexToken; //用于页面显示指数价格(正向=index_token,反则反之)

    private BigDecimal fundingLowerBound; //永续合约资金费率下限

    private BigDecimal fundingUpperBound; //永续合约资金费率下限

    private BigDecimal fundingInterest; //永续合约两币种借贷利率之和

    private Long createdAt;

    private Long updatedAt;

    private Integer isReverse;

    private BigDecimal marginPrecision; //用户修改保证金的最小精度

    private Integer updateStatus;

    @Data
    public static class RiskLimit {

        private BigDecimal riskLimitAmount;

        @JsonSerialize(using = PercentageOutputSerialize.class)
        private BigDecimal maintainMargin;

        @JsonSerialize(using = PercentageOutputSerialize.class)
        private BigDecimal initialMargin;
    }

    @Data
    public static class SymbolNameLocale {

        private String locale;

        private String name;
    }


    public static SymbolFuturesRecordDTO parseFromProtoObj(ContractApplyObj applyObj) {
        String symbolNameLocaleJson = applyObj.getSymbolNameLocaleJson();
        String riskLimitJson = applyObj.getRiskLimitJson();
        List<SymbolNameLocale> nameLocaleList = new ArrayList<>();
        List<RiskLimit> riskLimitList = new ArrayList<>();
        String symbolName = applyObj.getSymbolName();
        if (StringUtils.isNotEmpty(symbolNameLocaleJson)) {
            nameLocaleList = JsonUtil.defaultGson().fromJson(symbolNameLocaleJson, new TypeToken<List<SymbolNameLocale>>() {}.getType());
            Map<String, SymbolNameLocale> symbolNameLocaleMap = nameLocaleList.stream().collect(Collectors.toMap(SymbolNameLocale::getLocale, Function.identity()));
            SymbolNameLocale nameLocal = Objects.nonNull(symbolNameLocaleMap.get("en-us"))? symbolNameLocaleMap.get("en-us"): symbolNameLocaleMap.get("zh-cn");
            if (Objects.nonNull(nameLocal)) {
                symbolName = nameLocal.getName();
            }
        }

        if (StringUtils.isNotEmpty(riskLimitJson)) {
            riskLimitList = JsonUtil.defaultGson().fromJson(riskLimitJson, new TypeToken<List<RiskLimit>>() {}.getType());
        }
        Combo2<BigDecimal, BigDecimal> overPriceRangeCombo2 = SymbolUtil.priceRangeFromString(applyObj.getOverPriceRange());
        Combo2<BigDecimal, BigDecimal> marketPriceRangeCombo2 = SymbolUtil.priceRangeFromString(applyObj.getMarketPriceRange());
        return SymbolFuturesRecordDTO.builder()
                .id(applyObj.getId())
                .symbolId(applyObj.getSymbolId())
                .symbolName(symbolName)
                .baseTokenId(applyObj.getBaseTokenId())
                .quoteTokenId(applyObj.getQuoteTokenId())
                .underlyingId(applyObj.getUnderlyingId())
                .displayUnderlyingId(applyObj.getDisplayUnderlyingId())
                .symbolNameLocaleList(nameLocaleList)
                .riskLimitList(riskLimitList)
                .exchangeId(applyObj.getExchangeId())
                .brokerId(applyObj.getBrokerId())
                .minTradeQuantity(DecimalUtil.toBigDecimal(applyObj.getMinTradeQuantity()))
                .minTradeAmount(DecimalUtil.toBigDecimal(applyObj.getMinTradeAmount()))
                .minPricePrecision(DecimalUtil.toBigDecimal(applyObj.getMinPricePrecision()))
                .digitMergeList(applyObj.getDigitMergeList())
                .basePrecision(DecimalUtil.toBigDecimal(applyObj.getBasePrecision()))
                .quotePrecision(DecimalUtil.toBigDecimal(applyObj.getQuotePrecision()))
                .displayToken(applyObj.getDisplayToken())
                .currency(applyObj.getCurrency())
                .currencyDisplay(applyObj.getCurrencyDisplay())
                .contractMultiplier(DecimalUtil.toBigDecimal(applyObj.getContractMultiplier()))
                .limitDownInTradingHours(DecimalUtil.toBigDecimal(applyObj.getLimitDownInTradingHours()))
                .limitUpInTradingHours(DecimalUtil.toBigDecimal(applyObj.getLimitUpInTradingHours()))
                .limitDownOutTradingHours(DecimalUtil.toBigDecimal(applyObj.getLimitDownOutTradingHours()))
                .limitUpOutTradingHours(DecimalUtil.toBigDecimal(applyObj.getLimitUpOutTradingHours()))
                .maxLeverage(DecimalUtil.toBigDecimal(applyObj.getMaxLeverage()))
                .leverageRange(applyObj.getLeverageRange())
                .overPriceDownRange(Objects.isNull(overPriceRangeCombo2)? BigDecimal.ZERO: overPriceRangeCombo2.getV1())
                .overPriceUpRange(Objects.isNull(overPriceRangeCombo2)? BigDecimal.ZERO: overPriceRangeCombo2.getV2())
                .marketPriceDownRange(Objects.isNull(marketPriceRangeCombo2)? BigDecimal.ZERO: marketPriceRangeCombo2.getV1())
                .marketPriceUpRange(Objects.isNull(marketPriceRangeCombo2)? BigDecimal.ZERO: marketPriceRangeCombo2.getV2())
                .isPerpetualSwap(applyObj.getIsPerpetualSwap())
                .indexToken(applyObj.getIndexToken())
                .displayIndexToken(applyObj.getDisplayIndexToken())
                .fundingLowerBound(DecimalUtil.toBigDecimal(applyObj.getFundingLowerBound()))
                .fundingUpperBound(DecimalUtil.toBigDecimal(applyObj.getFundingUpperBound()))
                .fundingInterest(DecimalUtil.toBigDecimal(applyObj.getFundingInterest()))
                .createdAt(applyObj.getCreatedAt())
                .updatedAt(applyObj.getUpdatedAt())
                .isReverse(applyObj.getIsReverse())
                .marginPrecision(DecimalUtil.toBigDecimal(applyObj.getMarginPrecision()))
                .state(applyObj.getState())
                .build();
    }

    public static void main(String[] args) {

        String symbolNameLocaleJson = "[" +
                "{" +
                "\"locale\": \"zh\"," +
                "\"name\": \"合约\"" +
                "}," +
                "{" +
                "\"locale\": \"en\"," +
                "\"name\": \"swqp\"" +
                "}" +
                "]";

        if (StringUtils.isNotEmpty(symbolNameLocaleJson)) {
            List<SymbolNameLocale> nameLocaleList = JsonUtil.defaultGson().fromJson(symbolNameLocaleJson, new TypeToken<List<SymbolNameLocale>>() {}.getType());
            for (SymbolNameLocale s: nameLocaleList) {
                System.out.println(s.getName() + ": " + s.getLocale());
            }
        }
    }
}
