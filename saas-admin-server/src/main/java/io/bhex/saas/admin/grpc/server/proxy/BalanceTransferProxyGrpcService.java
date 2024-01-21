package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.BatchTransferRequest;
import io.bhex.base.account.BatchTransferResponse;
import io.bhex.base.account.BatchTransferServiceGrpc;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;

import javax.annotation.Resource;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.server.proxy
 * @Author: ming.xu
 * @CreateDate: 14/11/2018 5:30 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@GrpcService
public class BalanceTransferProxyGrpcService extends BatchTransferServiceGrpc.BatchTransferServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private BatchTransferServiceGrpc.BatchTransferServiceBlockingStub getTransferStub() {
        return grpcConfig.batchTransferServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void batchTransfer(BatchTransferRequest request, StreamObserver<BatchTransferResponse> responseObserver) {
        BatchTransferResponse reply = getTransferStub().batchTransfer(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
