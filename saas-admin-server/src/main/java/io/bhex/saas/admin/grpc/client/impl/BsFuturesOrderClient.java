package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.broker.common.exception.BrokerErrorCode;
import io.bhex.broker.common.exception.BrokerException;
import io.bhex.broker.grpc.order.*;
import io.bhex.broker.grpc.statistics.QueryOrgTradeDetailRequest;
import io.bhex.broker.grpc.statistics.QueryOrgTradeDetailResponse;
import io.bhex.broker.grpc.statistics.StatisticsServiceGrpc;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.grpc.Deadline;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ProjectName: broker
 * @Package: io.bhex.bhop.common.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/9/18 5:47 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class BsFuturesOrderClient {

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    private FuturesOrderServiceGrpc.FuturesOrderServiceBlockingStub getFuturesOrderStub(long orgId) {
        return FuturesOrderServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }

    public QueryFuturesOrdersResponse queryFuturesOrders(QueryFuturesOrdersRequest request) {
        try {
            QueryFuturesOrdersResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).queryFuturesOrders(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }

    public FuturesPositionsResponse getFuturesPositions(FuturesPositionsRequest request) {
        try {
            FuturesPositionsResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).getFuturesPositions(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }

    public QueryMatchResponse queryFuturesMatchInfo(QueryMatchRequest request) {
        try {
            QueryMatchResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).queryFuturesMatch(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }

    public GetOrderMatchResponse getFuturesOrderMatchInfo(GetOrderMatchRequest request) {
        try {
            GetOrderMatchResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).getFuturesOrderMatch(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }


    public GetOrderResponse getFuturesOrder(GetOrderRequest request) {
        try {
            GetOrderResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).getFuturesOrder(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }


    public FuturesPosition getLiquidationPosition(QueryLiquidationPositionRequest request) {
        try {
            QueryLiquidationPositionResponse response = getFuturesOrderStub(request.getHeader().getOrgId()).queryLiquidationPosition(request);
            if (response.getRet() != 0) {
                throw new BrokerException(BrokerErrorCode.fromCode(response.getRet()));
            }
            return response.getFuturesPosition();
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }

    public List<QueryOrgTradeDetailResponse.TradeDetail> getContractTrades(QueryOrgTradeDetailRequest request) {
        return StatisticsServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(request.getHeader().getOrgId()))
                .queryOrgTradeDetail(request).getTradeDetailList();
    }

}
