package io.bhex.saas.admin.controller.dto;

import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.SymbolDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolDTO {

    private String symbolId;
    private String symbolName;
    private String baseTokenId;
    private String quoteTokenId;
    private BigDecimal minTradeQuantity;
    private BigDecimal minTradeAmount;
    private BigDecimal minPricePrecision;
    private BigDecimal basePrecision;
    private BigDecimal quotePrecision;
    private Boolean allowTrade;
    private Boolean published;
    private String depthMerge;

    public static SymbolDTO parseSymbolDetail(SymbolDetail symbolDetail) {
        return SymbolDTO.builder()
            .symbolId(symbolDetail.getSymbolId())
            .symbolName(symbolDetail.getSymbolName())
            .baseTokenId(symbolDetail.getBaseTokenId())
            .quoteTokenId(symbolDetail.getQuoteTokenId())
            .minTradeQuantity(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeQuantity()))
            .minTradeAmount(DecimalUtil.toBigDecimal(symbolDetail.getMinTradeAmount()))
            .minPricePrecision(DecimalUtil.toBigDecimal(symbolDetail.getMinPricePrecision()))
            .basePrecision(DecimalUtil.toBigDecimal(symbolDetail.getBasePrecision()))
            .quotePrecision(DecimalUtil.toBigDecimal(symbolDetail.getQuotePrecision()))
            .depthMerge(symbolDetail.getDigitMergeList())
            .build();
    }
}
