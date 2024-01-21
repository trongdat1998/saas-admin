package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-24 16:03
 */
@Data
public class BindMarginFundingPO {
    @NotNull
    public Long orgId;

    @NotNull
    public Long accountId;
}
