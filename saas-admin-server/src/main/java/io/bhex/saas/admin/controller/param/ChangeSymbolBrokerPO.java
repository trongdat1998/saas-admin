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
public class ChangeSymbolBrokerPO {
    @NotNull(message = "token.record.exchangeId.required")
    private Long brokerId;
    @NotNull(message = "symbol.record.symbol.required")
    private String symbolId;
    @NotNull(message = "token.record.toExchangeId.required")
    private Long toBrokerId;

}
