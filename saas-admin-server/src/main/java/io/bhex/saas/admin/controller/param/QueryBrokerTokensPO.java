package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryBrokerTokensPO {

    private Long exchangeId;

    @NotNull
    private Long brokerId;

    private Integer current;

    private Integer pageSize;

    private Integer category;

    private String token = null;

}
