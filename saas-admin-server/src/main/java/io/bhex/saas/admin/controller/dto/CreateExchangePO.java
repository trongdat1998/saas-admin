package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bhex.bhop.common.util.percent.Percentage;
import io.bhex.bhop.common.util.percent.PercentageInputDeserialize;
import io.bhex.bhop.common.util.validation.CommonInputValid;
import io.bhex.bhop.common.util.validation.PhoneValid;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.math.BigDecimal;

/**
 * @Description: create exchange parameter object
 * @Date: 2018/8/19 上午10:36
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class CreateExchangePO {


    @NotEmpty
    @CommonInputValid
    private String exchangeName;


    private String country;

//    @DecimalMin(value = "0")
//    @DecimalMax(value = "50")
//    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
//    private BigDecimal saasFeeRate;

    @CommonInputValid
    private String company;
    @CommonInputValid
    private String contactName;

    private String nationalCode;
    @PhoneValid
    private String contactTelephone;

    @NotEmpty
    @Email(regexp="(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;

    @CommonInputValid
    private String remark;

    private Integer instanceId;

    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
    @JsonDeserialize(using = PercentageInputDeserialize.class)
    @Percentage(min = "0", max = "100")
    private BigDecimal saasFee;

}
