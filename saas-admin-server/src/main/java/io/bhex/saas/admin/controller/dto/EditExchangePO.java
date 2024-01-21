package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bhex.bhop.common.util.percent.Percentage;
import io.bhex.bhop.common.util.percent.PercentageInputDeserialize;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class EditExchangePO {

    private Long id;

//    @NotNull(message = "saasFeeRate not null")
//    private BigDecimal saasFeeRate;

    @NotEmpty
    private String exchangeName;

    @NotNull(message = "company not null")
    private String company;


    private String email;

    @NotNull(message = "contactName not null")
    private String contactName;

    @NotNull(message = "contactTelephone not null")
    private String contactTelephone;

    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
    @JsonDeserialize(using = PercentageInputDeserialize.class)
    @Percentage(min = "0", max = "100")
    private BigDecimal saasFee;


    private String remark;
}
