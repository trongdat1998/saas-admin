package io.bhex.saas.admin.model;

import io.bhex.base.bhadmin.SymbolApplyObj;
import io.bhex.base.proto.DecimalUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Table(name = "tb_symbol_apply")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolApplyRecord {
    @Id
    private Long id;
    private Long exchangeId;
    private Long brokerId;
    private Integer state;

    private String symbolId;
    private String baseTokenId;
    private String quoteTokenId;

    private BigDecimal minPricePrecision;
    private BigDecimal basePrecision;
    private BigDecimal minTradeQuantity;

    private BigDecimal quotePrecision;
    private BigDecimal minTradeAmt;
    private String mergeDigitDepth;

    private String reason;

    private Long onlineTime;

    @ColumnType(jdbcType = JdbcType.TIMESTAMP)
    private Date createAt;
    @ColumnType(jdbcType = JdbcType.TIMESTAMP)
    private Date updateAt;

    public SymbolApplyObj toProtoObj() {
        return SymbolApplyObj.newBuilder()
            .setId(this.id)
            .setExchangeId(this.exchangeId)
            .setBrokerId(this.brokerId)
            .setState(this.state)
            .setSymbolId(this.symbolId)
            .setBaseTokenId(this.baseTokenId)
            .setQuoteTokenId(this.quoteTokenId)
            .setMinPricePrecision(DecimalUtil.fromBigDecimal(this.minPricePrecision))
            .setBasePrecision(DecimalUtil.fromBigDecimal(this.basePrecision))
            .setMinTradeQuantity(DecimalUtil.fromBigDecimal(this.minTradeQuantity))
            .setQuotePrecision(DecimalUtil.fromBigDecimal(this.quotePrecision))
            .setMinTradeAmt(DecimalUtil.fromBigDecimal(this.minTradeAmt))
            .setMergeDigitDepth(this.mergeDigitDepth)
            .setReason(this.reason)
            .setCreateAt(createAt.getTime())
            .setUpdateAt(createAt.getTime())
            .build();
    }

    public static SymbolApplyRecord parseFromProtoObj(SymbolApplyObj applyObj) {
        return SymbolApplyRecord.builder()
            .id(applyObj.getId())
            .exchangeId(applyObj.getExchangeId())
            .brokerId(applyObj.getBrokerId())
            .state(applyObj.getState())
            .symbolId(applyObj.getSymbolId())
            .baseTokenId(applyObj.getBaseTokenId())
            .quoteTokenId(applyObj.getQuoteTokenId())
            .minPricePrecision(DecimalUtil.toBigDecimal(applyObj.getMinPricePrecision()))
            .basePrecision(DecimalUtil.toBigDecimal(applyObj.getBasePrecision()))
            .minTradeQuantity(DecimalUtil.toBigDecimal(applyObj.getMinTradeQuantity()))
            .quotePrecision(DecimalUtil.toBigDecimal(applyObj.getQuotePrecision()))
            .minTradeAmt(DecimalUtil.toBigDecimal(applyObj.getMinTradeAmt()))
            .mergeDigitDepth(applyObj.getMergeDigitDepth())
            .reason(applyObj.getReason())
            .createAt(applyObj.getCreateAt() == 0 ? null : new Date(applyObj.getCreateAt()))
            .updateAt(applyObj.getUpdateAt() == 0 ? null : new Date(applyObj.getUpdateAt()))
               .onlineTime(applyObj.getOnlineTime())
            .build();
    }
}
