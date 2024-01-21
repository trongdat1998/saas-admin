package io.bhex.saas.admin.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 17:19
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class MarginFundingCrossDTO {
    private Long orgId;
    private String tokenId;
    private String accounts;
}
