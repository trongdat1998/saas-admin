package io.bhex.saas.admin.controller.dto;

import lombok.Data;

@Data
public class QueryExCommissionsPO {
    private Long fromTime;
    private Long endTime;
    private String exchangeName;

    private Long fromId;
    private Long lastId;

    private Boolean next;

    private Integer pageSize;
}
