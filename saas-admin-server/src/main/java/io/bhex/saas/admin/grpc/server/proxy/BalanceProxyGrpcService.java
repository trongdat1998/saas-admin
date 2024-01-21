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
 * @Date: 2018/11/1 上午10:24
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class BalanceProxyGrpcService extends BalanceServiceGrpc.BalanceServiceImplBase{

    @Resource
    GrpcClientConfig grpcConfig;

    private BalanceServiceGrpc.BalanceServiceBlockingStub getStub(){
        return grpcConfig.balanceServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void getBalanceDetail(GetBalanceDetailRequest request, StreamObserver<BalanceDetailList> responseObserver) {
        BalanceDetailList bhreply = getStub().getBalanceDetail(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getBalanceFlows(GetBalanceFlowsRequest request, StreamObserver<BalanceFlowsReply> responseObserver) {
        BalanceFlowsReply bhreply = getStub().getBalanceFlows(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getBalanceFlowsWithPage(GetBalanceFlowsWithPageRequest request, StreamObserver<BalanceFlowsReply> responseObserver) {
        BalanceFlowsReply bhreply = getStub().getBalanceFlowsWithPage(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getBatchAccountBalance(GetBatchAccountBalanceRequest request, StreamObserver<GetBatchAccountBalanceReply> responseObserver) {
        GetBatchAccountBalanceReply bhreply = getStub().getBatchAccountBalance(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getPosition(GetPositionRequest request, StreamObserver<PositionResponseList> responseObserver) {
        PositionResponseList positionResponseList = getStub().getPosition(request);
        responseObserver.onNext(positionResponseList);
        responseObserver.onCompleted();
    }

    @Override
    public void unlockBalance(UnlockBalanceRequest request, StreamObserver<UnlockBalanceResponse> responseObserver) {
        UnlockBalanceResponse unlockBalanceResponse = getStub().unlockBalance(request);
        responseObserver.onNext(unlockBalanceResponse);
        responseObserver.onCompleted();
    }
}
