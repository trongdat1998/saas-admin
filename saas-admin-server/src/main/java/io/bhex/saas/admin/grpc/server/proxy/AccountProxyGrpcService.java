package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class AccountProxyGrpcService extends AccountServiceGrpc.AccountServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private AccountServiceGrpc.AccountServiceBlockingStub getStub(){
        return grpcConfig.accountServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void bindAccount(BindAccountRequest request, StreamObserver<BindAccountReply> responseObserver) {
        BindAccountReply bhreply = getStub().bindAccount(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void bindRelation(BindRelationRequest request, StreamObserver<BindRelationReply> responseObserver) {
        BindRelationReply bhreply = getStub().bindRelation(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getAccountInfo(GetAccountInfoRequest request, StreamObserver<GetAccountInfoReply> responseObserver) {
        GetAccountInfoReply bhreply = getStub().getAccountInfo(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }
}
