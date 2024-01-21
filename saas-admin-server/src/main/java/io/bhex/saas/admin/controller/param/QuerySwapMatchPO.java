package io.bhex.saas.admin.controller.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 2019/9/19 3:40 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class QuerySwapMatchPO extends GetBrokerUserPO {

    private Long orgId;

    private Long orderId;

    private Long fromTradeId;

    private Integer limit;

}
