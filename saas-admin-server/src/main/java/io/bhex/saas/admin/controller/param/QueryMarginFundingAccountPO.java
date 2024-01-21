package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-24 15:51
 */
@Data
public class QueryMarginFundingAccountPO {
    @NotNull
    public Long orgId;
}
