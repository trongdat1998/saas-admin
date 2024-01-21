package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.Positive;

@Data
public class QueryBrokerTransferWhitelistPO {
    @Positive
    private int current;

    @Positive
    private int pageSize;

    private String brokerName;

    private Long brokerId;
}
