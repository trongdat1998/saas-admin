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
public class ChangeTokenBrokerPO {
    @NotNull(message = "token.record.exchangeId.required")
    private Long brokerId;
    @NotNull(message = "token.record.tokenId.required")
    private String tokenId;
    @NotNull(message = "token.record.toExchangeId.required")
    private Long toBrokerId;
}
