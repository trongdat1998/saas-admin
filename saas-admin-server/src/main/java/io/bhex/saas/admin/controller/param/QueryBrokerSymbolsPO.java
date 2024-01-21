package io.bhex.saas.admin.controller.param;

import lombok.Data;

@Data
public class QueryBrokerSymbolsPO {
    private Long brokerId;
    private Integer current;
    private Integer pageSize;
    private Integer category = 1;
    private Boolean owner = false;
    private String symbol;
}
