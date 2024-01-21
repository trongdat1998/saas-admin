package io.bhex.saas.admin.http.param;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class DeleteMarketAccountPO {
    @NotNull
    private Long orgId;

    @NotNull
    private Long id;
}
