package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryPlatformBrokerTokensPO {

    private Long brokerId;

    private Long exchangeId;

    @NotNull
    private Integer current = 1;

    @NotNull
    private Integer pageSize = 100;

    @NotNull
    private Integer category = 1;

    private String token = null;

    private String tokenId = null;

}
