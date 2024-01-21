package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueryLiquidationPositionPO extends GetBrokerUserPO {

    private Long orgId;
    @NotNull
    private Long orderId;
}
