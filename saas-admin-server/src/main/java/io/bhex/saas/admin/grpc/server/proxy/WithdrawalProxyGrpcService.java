package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.SetWithdrawalAuditStatusRequest;
import io.bhex.base.account.SetWithdrawalAuditStatusResponse;
import io.bhex.base.account.WithdrawalServiceGrpc;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2018/11/2 下午3:01
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class WithdrawalProxyGrpcService extends WithdrawalServiceGrpc.WithdrawalServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private WithdrawalServiceGrpc.WithdrawalServiceBlockingStub getStub(){
        return grpcConfig.withdrawalServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }


    @Override
    public void setWithdrawalAuditStatus(SetWithdrawalAuditStatusRequest request,
                                         StreamObserver<SetWithdrawalAuditStatusResponse> responseObserver) {
        SetWithdrawalAuditStatusResponse bhreply = getStub().setWithdrawalAuditStatus(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }
}
