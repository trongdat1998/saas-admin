package io.bhex.saas.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.bhex.base.token.SymbolDetail;
import io.bhex.broker.grpc.common.Header;
import io.bhex.broker.grpc.order.*;
import io.bhex.broker.grpc.statistics.QueryOrgTradeDetailRequest;
import io.bhex.broker.grpc.statistics.QueryOrgTradeDetailResponse;
import io.bhex.saas.admin.controller.dto.FuturesPositionDTO;
import io.bhex.saas.admin.controller.dto.SwapMatchDTO;
import io.bhex.saas.admin.controller.dto.SwapOrderDTO;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.impl.BsFuturesOrderClient;
import io.bhex.saas.admin.util.FuturesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 2019/9/18 5:31 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Service
public class BsSwapOrderService {

    @Autowired
    private BsFuturesOrderClient futuresOrderClient;

    @Autowired
    private BrokerSymbolClient brokerSymbolClient;


    /**
     * 期货未成交委托
     */
    public List<SwapOrderDTO> queryCurrentOrders(Long orgId, Long userId, String futuresOrderType,
                                                 String symbolId, Long fromOrderId, Long endOrderId,
                                                 Long startTime, Long endTime, Integer limit, Boolean profitLossOrder) {
        PlanOrder.FuturesOrderType orderType = PlanOrder.FuturesOrderType.valueOf(futuresOrderType);
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        QueryFuturesOrdersRequest.Builder builder = QueryFuturesOrdersRequest.newBuilder()
                .setHeader(header)
                .setSymbolId(Strings.nullToEmpty(symbolId))
                .setFromId(fromOrderId)
                .setEndId(endOrderId)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setLimit(limit)
                .setOrderType(orderType)
                .setQueryType(OrderQueryType.CURRENT);
        if (profitLossOrder) {
            builder.addAllPlanOrderTypes(Lists.newArrayList(PlanOrder.PlanOrderTypeEnum.STOP_LONG_PROFIT, PlanOrder.PlanOrderTypeEnum.STOP_LONG_LOSS,
                    PlanOrder.PlanOrderTypeEnum.STOP_SHORT_PROFIT, PlanOrder.PlanOrderTypeEnum.STOP_SHORT_LOSS));
        }

        QueryFuturesOrdersResponse response = futuresOrderClient.queryFuturesOrders(builder.build());
        if (orderType == PlanOrder.FuturesOrderType.LIMIT) {
            return Lists.newArrayList(response.getOrdersList()).stream().map(order -> getFuturesOrderResult(order, orgId)).collect(Collectors.toList());
        } else if (orderType == PlanOrder.FuturesOrderType.STOP) {
            return Lists.newArrayList(response.getPlanOrdersList()).stream().map(this::getFuturesPlanOrderResult).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }


    private ConcurrentHashMap<String, String[]> symbolUnitMap = new ConcurrentHashMap<>();

    public String getUnit(String symbolId, Long orgId) {
        return getTokenId(symbolId, 1, orgId);
    }

    /**
     * @param symbolId
     * @param type     1-displayTokenId 2-quoteTokenId
     * @param orgId
     * @return
     */
    private String getTokenId(String symbolId, int type, Long orgId) {
        if (symbolUnitMap.containsKey(symbolId)) {
            String[] arr = symbolUnitMap.get(symbolId);
            return type == 1 ? arr[0] : arr[1];
        }

        SymbolDetail detail = brokerSymbolClient.getBhSymbol(symbolId);
        if (!detail.getDisplayTokenId().equals("")) {
            symbolUnitMap.put(symbolId, new String[]{detail.getDisplayTokenId(), detail.getQuoteTokenId()});
        }
        return type == 1 ? detail.getDisplayTokenId() : detail.getQuoteTokenId();
    }

    private SwapOrderDTO getFuturesOrderResult(Order order, Long orgId) {
        return FuturesUtil.getFuturesOrderResult(order, order.getSymbolName(), getUnit(order.getSymbolId(), orgId));
    }

    private SwapOrderDTO getFuturesPlanOrderResult(PlanOrder order) {
        if (order == null) {
            return SwapOrderDTO.builder().build();
        }
        return FuturesUtil.getFuturesPlanOrderResult(order, order.getSymbolName(), getUnit(order.getSymbolId(), order.getOrgId()));
    }


    /**
     * 期货当前持仓
     */
    public List<FuturesPositionDTO> getFuturesPositions(Long orgId, Long userId, String symbolId, Long fromBalanceId, Long endBalanceId, Integer limit) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        FuturesPositionsRequest positionsRequest = FuturesPositionsRequest
                .newBuilder()
                .setHeader(header)
                .addAllTokenIds(FuturesUtil.getTokenIds(symbolId))
                .setFromBalanceId(fromBalanceId != null ? fromBalanceId : 0)
                .setEndBalanceId(endBalanceId != null ? endBalanceId : 0)
                .setLimit(limit)
                .build();
        return futuresOrderClient.getFuturesPositions(positionsRequest)
                .getPositionsList()
                .stream()
                .filter(t -> new BigDecimal(t.getTotal()).compareTo(BigDecimal.ZERO) != 0)
                .map(t -> toPositionResult(t, orgId))
                .collect(Collectors.toList());
    }

    /**
     * 当前持仓
     *
     * @param t
     * @return
     */
    private FuturesPositionDTO toPositionResult(FuturesPosition t, Long orgId) {
//        String unit = basicService.queryFuturesCoinToken(header.getOrgId(), t.getTokenId());
        // todo: unit
        return FuturesPositionDTO.builder()
                .positionId(t.getPositionId())
                .accountId(t.getAccountId())
                .symbolId(t.getTokenId())
                .symbolName(t.getTokenId())

                .leverage(FuturesUtil.toQuantityScale(t.getLeverage()))
                .total(FuturesUtil.toQuantityScale(t.getTotal()))
                .positionValues(FuturesUtil.toAmountScale(t.getPositionValues()))
                .margin(FuturesUtil.toAmountScale(t.getMargin()))
                .minMargin(FuturesUtil.toAmountScale(t.getMinMargin()))

                .orderMargin(FuturesUtil.toAmountScale(t.getOrderMargin()))
                .avgPrice(FuturesUtil.toAmountScale(t.getAvgPrice()))
                .liquidationPrice(FuturesUtil.toAmountScale(t.getLiquidationPrice()))
                .marginRate(FuturesUtil.toAmountScale(t.getMarginRate()))

                .indices(FuturesUtil.toAmountScale(t.getIndices()))
                .available(FuturesUtil.toAmountScale(t.getAvailable()))
                .coinAvailable(FuturesUtil.toAmountScale(t.getCoinAvailable()))
                .isLong(t.getIsLong())
                .realisedPnl(FuturesUtil.toAmountScale(t.getRealisedPnl()))
                .unrealisedPnl(FuturesUtil.toAmountScale(t.getUnrealisedPnl()))
                .profitRate(t.getProfitRate())
                .unit(getUnit(t.getTokenId(), orgId))
                .quoteTokenId(getTokenId(t.getTokenId(), 2, orgId))
                .build();
    }

    /**
     * 期货历史委托
     *
     * @param futuresOrderType
     * @param symbolId
     * @param fromOrderId
     * @param endOrderId
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    public List<SwapOrderDTO> queryHistoryOrders(Long orgId, Long userId, String futuresOrderType,
                                                 String symbolId, Long fromOrderId, Long endOrderId,
                                                 Long startTime, Long endTime, Integer limit, Boolean profitLossOrder) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        PlanOrder.FuturesOrderType orderType = PlanOrder.FuturesOrderType.valueOf(futuresOrderType);
        QueryFuturesOrdersRequest.Builder builder = QueryFuturesOrdersRequest.newBuilder()
                .setHeader(header)
                .setAccountId(0L)
                .setSymbolId(symbolId)
                .setFromId(fromOrderId)
                .setEndId(endOrderId)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setLimit(limit)
                .setOrderType(orderType)
                .setQueryType(OrderQueryType.HISTORY);
        if (profitLossOrder) {
            builder.addAllPlanOrderTypes(Lists.newArrayList(PlanOrder.PlanOrderTypeEnum.STOP_LONG_PROFIT, PlanOrder.PlanOrderTypeEnum.STOP_LONG_LOSS,
                    PlanOrder.PlanOrderTypeEnum.STOP_SHORT_PROFIT, PlanOrder.PlanOrderTypeEnum.STOP_SHORT_LOSS));
        }
        QueryFuturesOrdersResponse response = futuresOrderClient.queryFuturesOrders(builder.build());
        if (orderType == PlanOrder.FuturesOrderType.LIMIT) {
            return Lists.newArrayList(response.getOrdersList()).stream().map(order -> getFuturesOrderResult(order, orgId)).collect(Collectors.toList());
        } else if (orderType == PlanOrder.FuturesOrderType.STOP) {
            return Lists.newArrayList(response.getPlanOrdersList()).stream().map(this::getFuturesPlanOrderResult).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public SwapOrderDTO queryFutureOrder(Long orgId, Long userId, long orderId) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        GetOrderRequest.Builder builder = GetOrderRequest.newBuilder()
                .setHeader(header)
                .setOrderId(orderId);

        GetOrderResponse response = futuresOrderClient.getFuturesOrder(builder.build());

        return getFuturesOrderResult(response.getOrder(), orgId);
    }


    /**
     * 期货历史成交
     *
     * @param orgId
     * @param userId
     * @param symbolId
     * @param fromTraderId
     * @param endTradeId
     * @param startTime
     * @param endTime
     * @param limit
     * @return
     */
    public List<SwapMatchDTO> queryMatchInfo(Long orgId, Long userId, String symbolId, Long fromTraderId, Long endTradeId,
                                             Long startTime, Long endTime, Integer limit) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        QueryMatchRequest request = QueryMatchRequest.newBuilder()
                .setHeader(header)
                .setAccountId(0L)
                .setSymbolId(symbolId)
                .setFromId(fromTraderId)
                .setEndId(endTradeId)
                .setStartTime(startTime)
                .setEndTime(endTime)
                .setLimit(limit)
                .build();
        QueryMatchResponse response = futuresOrderClient.queryFuturesMatchInfo(request);
        return Lists.newArrayList(response.getMatchList()).stream().map(matchInfo -> getMatchResult(matchInfo, orgId)).collect(Collectors.toList());
    }

    private SwapMatchDTO getMatchResult(MatchInfo matchInfo, Long orgId) {
        return SwapMatchDTO.builder()
                .accountId(matchInfo.getAccountId())
                .orderId(matchInfo.getOrderId())
                .tradeId(matchInfo.getTradeId())
                .symbolId(matchInfo.getSymbolId())
                .symbolName(matchInfo.getSymbolName())
                .baseTokenId(matchInfo.getBaseTokenId())
                .baseTokenName(matchInfo.getBaseTokenName())
                .quoteTokenId(matchInfo.getQuoteTokenId())
                .quoteTokenName(matchInfo.getQuoteTokenName())
                .price(matchInfo.getPrice())
                .quantity(matchInfo.getQuantity())
                .feeTokenId(matchInfo.getFee().getFeeTokenId())
                .feeTokenName(matchInfo.getFee().getFeeTokenName())
                .fee(matchInfo.getFee().getFee())
                .type(matchInfo.getOrderType() == OrderType.MARKET ? OrderType.MARKET.name() : OrderType.LIMIT.name())
                .side(FuturesUtil.getFuturesSide(matchInfo.getOrderSide(), matchInfo.getIsClose()).name())
                .time(matchInfo.getTime())
                .executedAmount((new BigDecimal(matchInfo.getPrice())
                        .multiply(new BigDecimal(matchInfo.getQuantity()))
                        .toPlainString()))
                .priceType(matchInfo.getFuturesPriceType())
                .pnl(matchInfo.getPnl())
                .unit(getUnit(matchInfo.getSymbolId(), orgId))
                .build();
    }

    /**
     * 期货成交详情
     *
     * @param orgId
     * @param userId
     * @param orderId
     * @param fromTradeId
     * @param limit
     * @return
     */
    public List<SwapMatchDTO> getOrderMatchInfo(Long orgId, Long userId, Long orderId, Long fromTradeId, Integer limit) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        GetOrderMatchRequest request = GetOrderMatchRequest.newBuilder()
                .setHeader(header)
                .setAccountId(0L)
                .setOrderId(orderId)
                .setFromId(fromTradeId)
                .setLimit(limit)
                .build();
        GetOrderMatchResponse response = futuresOrderClient.getFuturesOrderMatchInfo(request);
        return Lists.newArrayList(response.getMatchList()).stream().map(matchInfo -> getMatchResult(matchInfo, orgId)).collect(Collectors.toList());
    }

    public FuturesPositionDTO getLiquidationPosition(Long orgId, Long userId, Long accountId, Long orderId) {
        Header header = Header.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();
        QueryLiquidationPositionRequest request = QueryLiquidationPositionRequest.newBuilder()
                .setHeader(header)
                .setAccountId(accountId)
                .setOrderId(orderId)
                .build();
        FuturesPosition futuresPosition = futuresOrderClient.getLiquidationPosition(request);
        return toPositionResult(futuresPosition, orgId);
    }

    public List<QueryOrgTradeDetailResponse.TradeDetail> getContractTrades(QueryOrgTradeDetailRequest request) {
        return futuresOrderClient.getContractTrades(request);
    }
}
