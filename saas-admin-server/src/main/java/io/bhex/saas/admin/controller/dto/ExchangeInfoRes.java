package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.percent.PercentageOutputSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeInfoRes {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long exchangeId;

    private String exchangeName;

    //private BigDecimal saasFeeRate;

    private String company;

    private String email;

    private String contactName;

    private String contactTelephone;

    private Integer status;

    //private Integer payEarnest;

    private String remark;

    private int dnsSetting;

    @JsonSerialize(using = PercentageOutputSerialize.class)
    private BigDecimal saasFee;

    private Long createdAt;

    private String adminWebUrl;

    private boolean forbidAccessStatus;

    private Boolean verifying; //是否在审核中

}
