/**********************************
 *@项目名称: broker-parent
 *@文件名称: io.bhex.broker.domain
 *@Date 2018/6/26
 *@Author peiwei.ren@bhex.io 
 *@Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.controller.dto;

import io.bhex.broker.grpc.order.FuturesPriceType;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class SwapOrderDTO {

    //时间
    private Long time;
    // 订单ID
    private Long orderId;

    private Long executedOrderId;
    //账户ID
    private Long accountId;
    //账户类型
    private Integer accountType;
    private String clientOrderId;
    //币对Id
    private String symbolId;
    //币对Name
    private String symbolName;
    private String baseTokenId;
    private String baseTokenName;
    private String quoteTokenId;
    private String quoteTokenName;
    //下单价格
    private String price;
    //原始下单数量
    private String origQty;
    //成交量
    private String executedQty;
    //成交量
    private String executedAmount;

    // 成交均价
    private String avgPrice;

    //订单类型
    private String type;
    //买卖方向
    private String side;
    private @Singular("fee")
    List<OrderMatchFeeInfo> fees;
    //状态标识
    private String status;
    //状态标识
    private String statusDesc;

    private String lastExecutedQuantity;
    private String lastExecutedPrice;
    private String commissionAmount;
    private String commissionAsset;
    //正常情况Ticket是"非自成交". true=非自成交，false=是自成交
    private Boolean isNormal;

    //未成交数量
    private String noExecutedQty;
    //下单总金额
    private String amount;

    private Long exchangeId;

    private Long orgId;
    //期权类型：看涨，看跌
//    private OptionType optionType;
    //保证金
    private String margin;
    //期货：杠杆
    private String leverage;
    //期货：是否是平仓单
    private Boolean isClose;

    //期货: 计划委托-价格类型
    private FuturesPriceType priceType;
    //期货: 触发价格
    private String triggerPrice;
    //期货: 实际委托价格
    private String executedPrice;

    //期货：是否为系统强平单
    private Boolean isLiquidationOrder;

    //期货：爆仓单类型
    private String liquidationType; //IOC爆仓时候的强平单  ADL爆仓时候的减仓单

    private String unit;

    private String planOrderType;

    private Long updated;
}
