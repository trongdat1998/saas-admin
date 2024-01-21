package io.bhex.saas.admin.controller.param;

import lombok.Data;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 2019/9/19 11:08 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
public class QuerySwapOrderPO extends GetBrokerUserPO {

    private Long orgId;

    private String symbolId;

    private Long fromOrderId;

    private Long endOrderId;

    private String timeRange;

    private String futuresOrderType;

    private Integer limit;

    //止盈止损订单
    private Boolean profitLossOrder = false;
}
