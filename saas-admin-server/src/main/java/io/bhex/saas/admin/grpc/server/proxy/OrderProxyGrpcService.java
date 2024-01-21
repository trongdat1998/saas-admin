package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2018/11/2 下午2:54
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class OrderProxyGrpcService extends OrderServiceGrpc.OrderServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private OrderServiceGrpc.OrderServiceBlockingStub getStub(){
        return grpcConfig.orderServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void getOrders(GetOrdersRequest request, StreamObserver<GetOrdersReply> responseObserver) {
        GetOrdersReply bhreply = getStub().getOrders(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getTrades(GetTradesRequest request, StreamObserver<GetTradesReply> responseObserver) {
        GetTradesReply bhreply = getStub().getTrades(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void cancelOrder(CancelOrderRequest request, StreamObserver<CancelOrderReply> responseObserver) {
        CancelOrderReply bhreply = getStub().cancelOrder(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }


    @Override
    public void getTradesDetailDesc(GetOrderTradeDetailListRequest request, StreamObserver<GetOrderTradeDetailReply> responseObserver) {
        GetOrderTradeDetailReply orderTradeDetailReply = getStub().getTradesDetailDesc(request);
        responseObserver.onNext(orderTradeDetailReply);
        responseObserver.onCompleted();
    }
}
