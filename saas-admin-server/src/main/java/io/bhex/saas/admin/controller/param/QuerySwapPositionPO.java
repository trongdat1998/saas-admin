package io.bhex.saas.admin.controller.param;

import io.bhex.bhop.common.util.validation.SymbolValid;
import lombok.Data;

import javax.validation.constraints.Max;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 2019/9/19 3:35 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class QuerySwapPositionPO extends GetBrokerUserPO {

    private Long orgId;

    @SymbolValid(allowEmpty = true)
    private String symbolId;

    private Long fromPositionId;

    private Long endPositionId;

    @Max(value = 500)
    private Integer limit = 20;
}
