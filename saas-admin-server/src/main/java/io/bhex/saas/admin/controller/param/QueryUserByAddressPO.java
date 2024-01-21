package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-17 14:22
 */
@Data
public class QueryUserByAddressPO {

    public Long orgId = 0L;
    @NotEmpty
    public String tokenId;
    @NotEmpty
    public String address;
    public String addressTag;

}
