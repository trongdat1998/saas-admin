package io.bhex.saas.admin.http.param;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class QueryMarketAccountPO {
    @NotNull
    private Long orgId;

    private Long fromId;

    private Integer limit;
}
