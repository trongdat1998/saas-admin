package io.bhex.saas.admin.controller.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 2019/9/19 3:38 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class QuerySwapTradePO extends GetBrokerUserPO {

    private Long orgId;

    private String symbolId;

    private Long fromTradeId;

    private long endTradeId = 0;

    private String timeRange;

    private Integer limit;
}
