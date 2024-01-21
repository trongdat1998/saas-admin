package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class QueryExchangesPO {
    @Positive
    private int current;

    @Positive
    private int pageSize;

    private String exchangeName;

    private Long exchangeId;
}
