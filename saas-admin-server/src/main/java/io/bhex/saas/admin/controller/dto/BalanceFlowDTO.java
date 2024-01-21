package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.DecimalOutputSerialize;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @Description:财务数据
 * @Date: 2018/9/22 下午5:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class BalanceFlowDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;

    private String tokenId;
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal changed;//变动数量
    @JsonSerialize(using = DecimalOutputSerialize.class)
    private BigDecimal total;//变动后资金总额

    private int businessSubject;

    private long created;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long balanceFlowId;

}
