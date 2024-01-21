package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class AddCustromerSaasTransferPO {

    @NotNull
    private Integer authType;

    @NotEmpty
    private String verifyCode;
}
