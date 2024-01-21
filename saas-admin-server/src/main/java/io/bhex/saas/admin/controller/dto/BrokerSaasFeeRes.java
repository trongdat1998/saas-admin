package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BrokerSaasFeeRes {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long exchangeId;

    /**
     * 收取的Saas费率
     */
    private BigDecimal feeRate;

    /**
     * 生效时间
     */
    private String actionTime;
}
