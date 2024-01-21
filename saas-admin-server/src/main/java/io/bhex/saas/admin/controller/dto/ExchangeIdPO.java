package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ExchangeIdPO {
    @NotNull
    private Long exchangeId;
}
