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

@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class SwapMatchDTO {
    // 成交时间（撮合时间）
    private Long time;
    // 成交ID
    private Long tradeId;
    // 订单ID
    private Long orderId;

    private Long userId;
    // 账户ID
    private Long accountId;
    // 账户类型
    private Integer accountType;
    //币对
    private String symbolId;
    //币对
    private String symbolName;
    private String baseTokenId;
    private String baseTokenName;
    private String quoteTokenId;
    private String quoteTokenName;
    // 成交价
    private String price;
    // 成交量
    private String quantity;
    // 手续费token
    private String feeTokenId;
    // 手续费token
    private String feeTokenName;
    // 手续费
    private String fee;
    //订单类型
    private String type;
    //买卖方向
    private String side;

    //成交额
    private String executedAmount;
    //期权类型：看涨，看跌
//    private OptionType optionType;

    //期货: 价格类型
    private FuturesPriceType priceType;
//    //正常情况Ticket是"非自成交". true=非自成交，false=是自成交
//    private boolean isNormal;

    private String pnl; //成交盈亏

    private String unit;


}
