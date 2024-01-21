package io.bhex.saas.admin.model;

import io.bhex.base.bhadmin.ContractApplyObj;
import io.bhex.base.proto.DecimalUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * @ProjectName: exchange
 * @Package: io.bhex.ex.admingrpc.model
 * @Author: ming.xu
 * @CreateDate: 2019/10/9 5:22 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_contract_apply_record")
public class ContractApplyRecord {

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    private String symbolId;

    private String symbolName;

    private String baseTokenId;

    private String symbolNameLocaleJson;

    private String riskLimitJson;

    private String quoteTokenId;

    private String underlyingId; //标的id

    private String displayUnderlyingId; //展示用的标的id

    private Long exchangeId; //申请的交易所id

    private Long brokerId;

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

    private String overPriceRange; //超价浮动范围

    private String marketPriceRange; //市价浮动范围

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


    public ContractApplyObj toProtoObj() {
        return ContractApplyObj.newBuilder()
                .setId(this.id)
                .setSymbolId(this.symbolId)
                .setSymbolName(this.symbolName)
                .setBaseTokenId(this.baseTokenId)
                .setSymbolNameLocaleJson(this.symbolNameLocaleJson)
                .setRiskLimitJson(this.riskLimitJson)
                .setQuoteTokenId(this.quoteTokenId)
                .setUnderlyingId(this.underlyingId)
                .setDisplayUnderlyingId(this.displayUnderlyingId)
                .setExchangeId(this.exchangeId)
                .setBrokerId(this.brokerId)
                .setMinTradeQuantity(DecimalUtil.fromBigDecimal(this.minTradeQuantity))
                .setMinTradeAmount(DecimalUtil.fromBigDecimal(this.minTradeAmount))
                .setMinPricePrecision(DecimalUtil.fromBigDecimal(this.minPricePrecision))
                .setDigitMergeList(this.digitMergeList)
                .setBasePrecision(DecimalUtil.fromBigDecimal(this.basePrecision))
                .setQuotePrecision(DecimalUtil.fromBigDecimal(this.quotePrecision))
                .setDisplayToken(this.displayToken)
                .setCurrency(this.currency)
                .setCurrencyDisplay(this.currencyDisplay)
                .setContractMultiplier(DecimalUtil.fromBigDecimal(this.contractMultiplier))
                .setLimitDownInTradingHours(DecimalUtil.fromBigDecimal(this.limitDownInTradingHours))
                .setLimitUpInTradingHours(DecimalUtil.fromBigDecimal(this.limitUpInTradingHours))
                .setLimitDownOutTradingHours(DecimalUtil.fromBigDecimal(this.limitDownOutTradingHours))
                .setLimitUpOutTradingHours(DecimalUtil.fromBigDecimal(this.limitUpOutTradingHours))
                .setMaxLeverage(DecimalUtil.fromBigDecimal(this.maxLeverage))
                .setLeverageRange(this.leverageRange)
                .setOverPriceRange(this.overPriceRange)
                .setMarketPriceRange(this.marketPriceRange)
                .setIsPerpetualSwap(this.isPerpetualSwap)
                .setIndexToken(this.indexToken)
                .setDisplayIndexToken(this.displayIndexToken)
                .setFundingLowerBound(DecimalUtil.fromBigDecimal(this.fundingLowerBound))
                .setFundingUpperBound(DecimalUtil.fromBigDecimal(this.fundingUpperBound))
                .setFundingInterest(DecimalUtil.fromBigDecimal(this.fundingInterest))
                .setCreatedAt(this.createdAt)
                .setUpdatedAt(this.updatedAt)
                .setIsReverse(this.isReverse)
                .setMarginPrecision(DecimalUtil.fromBigDecimal(this.marginPrecision))
                .setState(this.state)
                .build();
    }

    public static ContractApplyRecord parseFromProtoObj(ContractApplyObj symbolRecord) {
        return ContractApplyRecord.builder()
                .id(symbolRecord.getId())
                .symbolId(symbolRecord.getSymbolId())
                .symbolName(symbolRecord.getSymbolName())
                .symbolNameLocaleJson(symbolRecord.getSymbolNameLocaleJson())
                .riskLimitJson(symbolRecord.getRiskLimitJson())
                .baseTokenId(symbolRecord.getBaseTokenId())
                .quoteTokenId(symbolRecord.getQuoteTokenId())
                .underlyingId(symbolRecord.getUnderlyingId())
                .displayUnderlyingId(symbolRecord.getDisplayUnderlyingId())
                .exchangeId(symbolRecord.getExchangeId())
                .brokerId(symbolRecord.getBrokerId())
                .minTradeQuantity(DecimalUtil.toBigDecimal(symbolRecord.getMinTradeQuantity()))
                .minTradeAmount(DecimalUtil.toBigDecimal(symbolRecord.getMinTradeAmount()))
                .minPricePrecision(DecimalUtil.toBigDecimal(symbolRecord.getMinPricePrecision()))
                .digitMergeList(symbolRecord.getDigitMergeList())
                .basePrecision(DecimalUtil.toBigDecimal(symbolRecord.getBasePrecision()))
                .quotePrecision(DecimalUtil.toBigDecimal(symbolRecord.getQuotePrecision()))
                .displayToken(symbolRecord.getDisplayToken())
                .currency(symbolRecord.getCurrency())
                .currencyDisplay(symbolRecord.getCurrencyDisplay())
                .contractMultiplier(DecimalUtil.toBigDecimal(symbolRecord.getContractMultiplier()))
                .limitDownInTradingHours(DecimalUtil.toBigDecimal(symbolRecord.getLimitDownInTradingHours()))
                .limitUpInTradingHours(DecimalUtil.toBigDecimal(symbolRecord.getLimitUpInTradingHours()))
                .limitDownOutTradingHours(DecimalUtil.toBigDecimal(symbolRecord.getLimitDownOutTradingHours()))
                .limitUpOutTradingHours(DecimalUtil.toBigDecimal(symbolRecord.getLimitUpOutTradingHours()))
                .maxLeverage(DecimalUtil.toBigDecimal(symbolRecord.getMaxLeverage()))
                .leverageRange(symbolRecord.getLeverageRange())
                .overPriceRange(symbolRecord.getOverPriceRange())
                .marketPriceRange(symbolRecord.getMarketPriceRange())
                .isPerpetualSwap(symbolRecord.getIsPerpetualSwap())
                .indexToken(symbolRecord.getIndexToken())
                .displayIndexToken(symbolRecord.getDisplayIndexToken())
                .fundingLowerBound(DecimalUtil.toBigDecimal(symbolRecord.getFundingLowerBound()))
                .fundingUpperBound(DecimalUtil.toBigDecimal(symbolRecord.getFundingUpperBound()))
                .fundingInterest(DecimalUtil.toBigDecimal(symbolRecord.getFundingInterest()))
                .createdAt(symbolRecord.getCreatedAt())
                .updatedAt(symbolRecord.getUpdatedAt())
                .isReverse(symbolRecord.getIsReverse())
                .marginPrecision(DecimalUtil.toBigDecimal(symbolRecord.getMarginPrecision()))
                .state(symbolRecord.getState())
                .build();
    }
}
