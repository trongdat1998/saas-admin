package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.BatchSyncTransferRequest;
import io.bhex.base.account.BatchTransferRequest;
import io.bhex.base.account.BatchTransferResponse;
import io.bhex.base.account.BatchTransferServiceGrpc;
import io.bhex.base.account.SyncTransferRequest;
import io.bhex.base.account.SyncTransferResponse;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2019/05/17
 * @Author: yuehao
 * @Copyright（C）: 2019 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class TransferProxyGrpcService extends BatchTransferServiceGrpc.BatchTransferServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private BatchTransferServiceGrpc.BatchTransferServiceBlockingStub getStub() {
        return grpcConfig.batchTransferServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void batchTransfer(BatchTransferRequest request, StreamObserver<BatchTransferResponse> responseObserver) {
        BatchTransferResponse batchTransferResponse = getStub().batchTransfer(request);
        responseObserver.onNext(batchTransferResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void syncTransfer(SyncTransferRequest request, StreamObserver<SyncTransferResponse> responseObserver) {
        SyncTransferResponse transferResponse = getStub().syncTransfer(request);
        responseObserver.onNext(transferResponse);
        responseObserver.onCompleted();
    }


    @Override
    public void batchSyncTransfer(BatchSyncTransferRequest request, StreamObserver<SyncTransferResponse> responseObserver) {
        SyncTransferResponse transferResponse = getStub().batchSyncTransfer(request);
        responseObserver.onNext(transferResponse);
        responseObserver.onCompleted();
    }
}
