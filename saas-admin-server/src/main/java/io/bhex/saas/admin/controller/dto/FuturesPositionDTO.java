package io.bhex.saas.admin.controller.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 期货持仓
 */
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
public class FuturesPositionDTO {

    private Long accountId; //账户id

    private Long positionId; //position id

    private String symbolId; //期货symbol id

    private String symbolName; //期货symbol name

    private String leverage; //杠杆

    private String total; //仓位(手)

    private String positionValues; //仓位价值（USDT）

    private String margin; //仓位保证金（USDT）

    private String minMargin; //最多减少保证金（USDT）

    private String orderMargin; //委托保证金

    private String avgPrice; //开仓均价。开仓均价 = 累计开仓价值 / （仓位 * 合约乘数）

    private String liquidationPrice; //预估强平价

    private String marginRate; //保证金率。保证金率 = 仓位保证金/仓位价值

    private String indices; //交割指数

    private String available; //可平量

    private String coinAvailable; //可用保证金

    private String isLong; //仓位方向: 1=多仓，0=空仓

    private String realisedPnl; //已实现盈亏

    private String unrealisedPnl; //未实现盈亏

    private String unit; //coin token unit

    private String quoteTokenId;

    private String profitRate; //持仓收益率



}
