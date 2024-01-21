package io.bhex.saas.admin.util;

import io.bhex.base.proto.DecimalUtil;
import io.bhex.broker.grpc.order.*;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.OrderMatchFeeInfo;
import io.bhex.saas.admin.controller.dto.SwapOrderDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FuturesUtil {

    public static String toAmountScale(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return toScale(value, BizConstant.FUTURES_AMOUNT_PRECISION);
    }

    public static String toQuantityScale(String value) {
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return toScale(value, BizConstant.FUTURES_QUANTITY_PRECISION);
    }

    public static String toScale(String stringValue, int newScale) {
        try {
            BigDecimal decimalValue = new BigDecimal(stringValue)
                    .setScale(newScale, RoundingMode.DOWN)
                    .stripTrailingZeros();
            return DecimalUtil.toTrimString(decimalValue);
        } catch (Exception e) {
            log.warn("toScale error. value:{}, newScale:{}", stringValue, newScale);
            return stringValue;
        }
    }

    public static SwapOrderDTO getFuturesOrderResult(Order order, String symbolName, String unit) {
        String statusCode = order.getStatusCode();
        log.info("getFuturesOrderResult: symbolName {} orderId {} feeSize {} orderType {}", symbolName, order.getOrderId(), order.getFeesCount(), order.getOrderType().name());
        List<OrderMatchFeeInfo> fees = order.getFeesList().stream()
                .map(fee -> OrderMatchFeeInfo.builder()
                        .feeTokenId(fee.getFeeTokenId())
                        .feeTokenName(fee.getFeeTokenName())
                        .fee(fee.getFee())
                        .build())
                .collect(Collectors.toList());

        return SwapOrderDTO.builder()
                .exchangeId(order.getExchangeId())
                .accountId(order.getAccountId())
                .orderId(order.getOrderId())
                .clientOrderId(order.getClientOrderId())
                .symbolId(order.getSymbolId())
                .symbolName(symbolName)
                .baseTokenId(order.getBaseTokenId())
                .baseTokenName(order.getBaseTokenName())
                .quoteTokenId(order.getQuoteTokenId())
                .quoteTokenName(order.getQuoteTokenName())
                .price(order.getPrice())
                .origQty(order.getOrigQty())
                .executedQty(order.getExecutedQty())
                .executedAmount(order.getExecutedAmount())
                .avgPrice(order.getAvgPrice())//成交均价
                .type(order.getOrderType() == OrderType.MARKET ? OrderType.MARKET.name() : OrderType.LIMIT.name())
//                .side(FuturesUtil.getFuturesSide(order.getOrderSide(), order.getIsClose()).name())
                .side(order.getFuturesOrderSide().name())
                .fees(fees)
                .status(statusCode)
                .time(order.getTime())
                .noExecutedQty(new BigDecimal(order.getOrigQty())
                        .subtract(new BigDecimal(order.getExecutedQty())).toPlainString())
                .amount(new BigDecimal(order.getOrigQty())
                        .multiply(new BigDecimal(order.getPrice())).toPlainString())
                .leverage(order.getLeverage())//扛杆
                .isClose(order.getIsClose())
                .margin(order.getOrderMarginLocked())//下单锁定的保证金
                .priceType(order.getFuturesPriceType())
                .isLiquidationOrder(order.getIsLiquidationOrder())
                .liquidationType(order.getLiquidationType().name())
                .unit(unit)
                .updated(order.getLastUpdated())
                .build();
    }

    public static SwapOrderDTO getFuturesPlanOrderResult(PlanOrder order, String symbolName, String unit) {
        return SwapOrderDTO.builder()
                .time(order.getTime())
                .orderId(order.getOrderId())
                .executedOrderId(order.getExecutedOrderId())
                .accountId(order.getAccountId())
                .clientOrderId(order.getClientOrderId())
                .symbolId(order.getSymbolId())
                .symbolName(symbolName)
                .baseTokenId(order.getBaseTokenId())
                .baseTokenName(order.getBaseTokenName())
                .quoteTokenId(order.getQuoteTokenId())
                .quoteTokenName(order.getQuoteTokenName())
                .leverage(order.getLeverage())//扛杆
                .priceType(order.getPriceType())//类型
                .side(order.getSide().name())//方向。包括：开仓买入=BUY_OPEN, 开仓卖出=SELL_OPEN, 平仓买入=BUY_CLOSE, 平仓卖出=SELL_CLOSE
                .triggerPrice(order.getTriggerPrice())//触发价格
                .price(order.getPrice())//委托价格
                .origQty(order.getOrigQty())//委托数量
                .executedPrice(order.getExecutedPrice())//实际委托价格
                .status(order.getStatus().name())//状态. STOP_TODO=等待委托，STOP_DONE=已委托，STOP_FAIL=委托失败
                .orgId(order.getOrgId())
                .exchangeId(order.getExchangeId())
                .type(PlanOrder.FuturesOrderType.STOP.name())
                .unit(unit)
                .planOrderType(order.getOrderType().name())
                .updated(order.getLastUpdated())
                .build();
    }

    public static List<String> getTokenIds(String tokenIds) {
        if (StringUtils.isEmpty(tokenIds)) {
            return new ArrayList<>();
        }
        return Arrays.asList(tokenIds.split(","));
    }

    public static FuturesOrderSide getFuturesSide(OrderSide orderSide, boolean isClose) {
        if (isClose) {
            return orderSide == OrderSide.BUY ? FuturesOrderSide.BUY_CLOSE : FuturesOrderSide.SELL_CLOSE;
        }
        return orderSide == OrderSide.BUY ? FuturesOrderSide.BUY_OPEN : FuturesOrderSide.SELL_OPEN;
    }
}
