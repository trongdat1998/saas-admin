package io.bhex.saas.admin.controller.dto;

import io.bhex.base.bhadmin.SymbolApplyObj;
import io.bhex.base.exadmin.SymbolRecord;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.SymbolDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SymbolRecordDTO {
    private Long id;
    private Integer state;
    private Long exchangeId;
    private Long brokerId;
    private String orgName;

    private String symbolId;
    private String baseToken;
    private String quoteToken;

    private BigDecimal minPricePrecision;
    private BigDecimal basePrecision;
    private BigDecimal minTradeQuantity;

    private BigDecimal quotePrecision;
    private BigDecimal minTradeAmt;
    private String mergeDigitDepth;

    private String reason;

    private Long createAt;
    private Long updateAt;
    private Boolean isPrivateSymbol;
    private Boolean isShareAble;
    private Integer updateStatus;

    public SymbolRecord toSymbolRecord() {
        return SymbolRecord.newBuilder()
            .setId(this.id)
            .setExchangeId(this.exchangeId)
            .setState(this.state)
            .setSymbolId(this.symbolId)
            .setBaseToken(this.baseToken)
            .setQuoteToken(this.quoteToken)
            .setMinPricePrecision(DecimalUtil.fromBigDecimal(this.minPricePrecision))
            .setBasePrecision(DecimalUtil.fromBigDecimal(this.basePrecision))
            .setMinTradeQuantity(DecimalUtil.fromBigDecimal(this.minTradeQuantity))
            .setQuotePrecision(DecimalUtil.fromBigDecimal(this.quotePrecision))
            .setMinTradeAmt(DecimalUtil.fromBigDecimal(this.minTradeAmt))
            .setMergeDigitDepth(this.mergeDigitDepth)
            .setReason(this.reason)
            .setCreateAt(this.createAt)
            .setUpdateAt(this.updateAt)
            .build();
    }

    public static SymbolRecordDTO parseSymbolRecord(SymbolApplyObj applyObj) {

        return SymbolRecordDTO.builder()
            .id(applyObj.getId())
            .exchangeId(applyObj.getExchangeId())
            .brokerId(applyObj.getBrokerId())
            .state(applyObj.getState())
            .symbolId(applyObj.getSymbolId())
            .baseToken(applyObj.getBaseTokenId())
            .quoteToken(applyObj.getQuoteTokenId())
            .minPricePrecision(DecimalUtil.toBigDecimal(applyObj.getMinPricePrecision()))
            .basePrecision(DecimalUtil.toBigDecimal(applyObj.getBasePrecision()))
            .minTradeQuantity(DecimalUtil.toBigDecimal(applyObj.getMinTradeQuantity()))
            .quotePrecision(DecimalUtil.toBigDecimal(applyObj.getQuotePrecision()))
            .minTradeAmt(DecimalUtil.toBigDecimal(applyObj.getMinTradeAmt()))
            .mergeDigitDepth(applyObj.getMergeDigitDepth())
            .reason(applyObj.getReason())
            .createAt(applyObj.getCreateAt())
            .updateAt(applyObj.getUpdateAt())
            .isShareAble(applyObj.getIsShareAble())
            .build();
    }

    public static SymbolRecordDTO parseSymbolDetail(SymbolDetail symbolDetail) {
        if (symbolDetail == null) {
            return SymbolRecordDTO.builder().build();
        }
        return SymbolRecordDTO.builder()
            .exchangeId(symbolDetail.getExchangeId())
            .symbolId(symbolDetail.getSymbolId())
            .baseToken(symbolDetail.getBaseTokenId())
            .quoteToken(symbolDetail.getQuoteTokenId())
            .minPricePrecision(DecimalUtil.toBigDecimal(symbolDetail.getMinPricePrecision()))
            .basePrecision(DecimalUtil.toBigDecimal(symbolDetail.getBasePrecision()))
            .minTradeQuantity(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeQuantity()))
            .quotePrecision(DecimalUtil.toBigDecimal(symbolDetail.getQuotePrecision()))
            .minTradeAmt(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeAmount()))
            .mergeDigitDepth(symbolDetail.getDigitMergeList())
            .build();
    }
}
