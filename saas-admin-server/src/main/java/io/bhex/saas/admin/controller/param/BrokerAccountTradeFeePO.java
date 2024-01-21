package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.bhex.bhop.common.util.percent.Percentage;
import io.bhex.bhop.common.util.percent.PercentageInputDeserialize;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description:
 * @Date: 2018/11/21 下午5:38
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class BrokerAccountTradeFeePO   implements Serializable {

    @NotNull
    private Long brokerId;


    @NotEmpty
    private String accountIds;

    //maker折扣比例
    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
    @JsonDeserialize(using = PercentageInputDeserialize.class)
    @Percentage(min = "0", max = "100")
    private BigDecimal makerFeeAdjust;

    //taker折扣比例
    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
    @JsonDeserialize(using = PercentageInputDeserialize.class)
    @Percentage(min = "0", max = "100")
    private BigDecimal takerFeeAdjust;


    private String accountId;

    private String remark;
}
