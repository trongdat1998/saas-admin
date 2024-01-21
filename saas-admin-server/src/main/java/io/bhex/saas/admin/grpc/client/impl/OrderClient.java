package io.bhex.saas.admin.grpc.client.impl;

import com.google.protobuf.TextFormat;
import io.bhex.base.account.*;
import io.bhex.broker.grpc.order.CancelOrderResponse;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.Deadline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class OrderClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    private OrderServiceGrpc.OrderServiceBlockingStub getStub() {
        return grpcConfig.orderServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private io.bhex.broker.grpc.order.OrderServiceGrpc.OrderServiceBlockingStub getBrokerOrderStub(long brokerId) {
        return io.bhex.broker.grpc.order.OrderServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(brokerId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }

    public CancelMatchOrderReply cancelBrokerOrders(Long brokerId, long exchangeId, String symbolId) {
        CancelMatchOrderRequest request = CancelMatchOrderRequest .newBuilder()
                .setBrokerId(brokerId).setSymbolId(symbolId).setExchangeId(exchangeId)
                .build();
        CancelMatchOrderReply reply = getStub().cancelBrokerOrders(request);
        log.info("cancelBrokerOrders req:{} res:{}", TextFormat.shortDebugString(request), reply);
        return reply;
    }

    public List<BookOrderStruct> getBookOrders(GetBookOrdersRequest request) {
        return grpcConfig.orderServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME).getBookOrders(request).getBookOrdersList();
    }

    public CancelOrderResponse cancelOrder(io.bhex.broker.grpc.order.CancelOrderRequest request, long brokerId) {
        io.bhex.broker.grpc.order.OrderServiceGrpc.OrderServiceBlockingStub stub = getBrokerOrderStub(brokerId);
        CancelOrderResponse reply = stub.cancelOrder(request);
        return reply;
    }

    /**
    * brokerId=0 撤掉所有券商的此币对
     */
    public CancelSymbolOrdersReply cancelBrokerOrderNew(long brokerId, String symbolId) {
        CancelSymbolOrdersRequest request = CancelSymbolOrdersRequest.newBuilder()
                .setBrokerId(brokerId).setSymbolId(symbolId)
                .build();
        return getStub().cancelSymbolOrders(request);
    }


}
