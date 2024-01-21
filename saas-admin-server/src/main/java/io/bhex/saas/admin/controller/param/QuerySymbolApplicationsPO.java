package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QuerySymbolApplicationsPO {


    @NotNull
    private Integer state;

    private long brokerId = 0L;

    @NotNull
    private Integer current = 1;

    @NotNull
    private Integer pageSize = 100;

    private String symbol = null;

}
