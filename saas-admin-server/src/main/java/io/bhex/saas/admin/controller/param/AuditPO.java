package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bhex.bhop.common.util.percent.PercentageOutputSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditPO {
    @NotNull(message = "audit.curState.required")
    private Integer curState;
    @NotNull(message = "audit.toState.required")
    private Integer toState;
    @NotNull(message = "audit.id.required")
    private Long id;
    private String reason;

    private String symbolId;
    @NotEmpty(message = "symbol.record.baseToken.required")
    private String baseToken;
    @NotEmpty(message = "symbol.record.quoteToken.required")
    private String quoteToken;
    @NotNull(message = "symbol.record.minPricePrecision.required")
    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal minPricePrecision; //价格精度
    @NotNull(message = "symbol.record.basePrecision.required")
    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal basePrecision;//数量精度
    @NotNull(message = "symbol.record.minTradeQuantity.required")
    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal minTradeQuantity; //最小交易数量
    @NotNull(message = "symbol.record.quotePrecision.required")
    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal quotePrecision;//交易额精度
    @NotNull(message = "symbol.record.minTradeAmt.required")
    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal minTradeAmt;//最小交易额
    @NotEmpty(message = "symbol.record.mergeDigitDepth.required")
    private String mergeDigitDepth;
}
