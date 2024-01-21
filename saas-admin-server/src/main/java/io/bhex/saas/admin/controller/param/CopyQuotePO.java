package io.bhex.saas.admin.controller.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CopyQuotePO {
    @NotNull(message = "token.record.exchangeId.required")
    private Long fromExchangeId;
    @NotNull(message = "token.record.toExchangeId.required")
    private Long toExchangeId;
    @NotNull(message = "symbol.record.symbol.required")
    private String symbolId;
}
