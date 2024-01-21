package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateTradeFeeSettingPO {

    private Long id;

    /**
     * 交易所或者券商id
     */
    private Long orgId;

    /**
     * 收取的Saas费率
     */
    private BigDecimal feeRate;

    /**
     * 生效时间
     */
    private Long actionTime;
}
