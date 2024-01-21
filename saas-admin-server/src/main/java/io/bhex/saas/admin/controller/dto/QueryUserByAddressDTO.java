package io.bhex.saas.admin.controller.dto;

import lombok.Data;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-17 14:25
 */
@Data
public class QueryUserByAddressDTO {
    public Long orgId;
    public Long userId;
    public Long accountId;
    public String tokenId;

}
