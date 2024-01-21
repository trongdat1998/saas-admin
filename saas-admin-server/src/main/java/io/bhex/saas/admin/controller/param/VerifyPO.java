package io.bhex.saas.admin.controller.param;

import io.bhex.bhop.common.util.validation.CommonInputValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VerifyPO {

    @NotNull
    private Long id;

    @CommonInputValid
    private String reviewComments;

    @NotNull
    private Boolean passed;

    private Integer authType;

    private String verifyCode;

}
