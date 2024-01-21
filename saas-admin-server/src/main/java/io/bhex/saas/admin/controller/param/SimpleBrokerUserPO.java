package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SimpleBrokerUserPO {

    @NotNull
    private Long orgId;

    private String brokerName;

    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
    private Long userId;

    private String email;

    private String remark;
}
