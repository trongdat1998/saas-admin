package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.clear.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2018/10/31 下午8:42
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class CommissionProxyGrpcService extends CommissionServiceGrpc.CommissionServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    @Override
    public void getCommisson(SaasRequest request, StreamObserver<CommissionResponse> responseObserver) {
        CommissionServiceGrpc.CommissionServiceBlockingStub clearStub = grpcConfig.clearCommissionServiceBlockingStub(GrpcClientConfig.CLEAR_CHANNEL_NAME);

        CommissionResponse reply = clearStub.getCommisson(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getClearHistory(SaasRequest request, StreamObserver<ClearHistoryResponse> responseObserver) {
        CommissionServiceGrpc.CommissionServiceBlockingStub clearStub = grpcConfig.clearCommissionServiceBlockingStub(GrpcClientConfig.CLEAR_CHANNEL_NAME);
        ClearHistoryResponse reply = clearStub.getClearHistory(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
