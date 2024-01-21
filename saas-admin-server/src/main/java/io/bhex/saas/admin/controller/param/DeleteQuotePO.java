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
public class DeleteQuotePO {
    @NotNull(message = "token.record.exchangeId.required")
    private Long exchangeId;
    @NotNull(message = "symbol.record.symbol.required")
    private String symbol;
}
