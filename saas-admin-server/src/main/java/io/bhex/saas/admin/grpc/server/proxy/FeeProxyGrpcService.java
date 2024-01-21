package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.*;
import io.bhex.base.exception.ErrorStatusRuntimeException;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import javax.annotation.Resource;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.server.proxy
 * @Author: ming.xu
 * @CreateDate: 16/11/2018 11:44 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@GrpcService
public class FeeProxyGrpcService extends FeeServiceGrpc.FeeServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private FeeServiceGrpc.FeeServiceBlockingStub getFeeStup() {
        return grpcConfig.saasFeeServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void updateExCommissionFee(UpdateExCommissionFeeRequest request, StreamObserver<UpdateExCommissionFeeResponse> responseObserver) {
        UpdateExCommissionFeeResponse response;
        try{
            response = getFeeStup().updateExCommissionFee(request);
        }
        catch (StatusRuntimeException e){
            response = UpdateExCommissionFeeResponse.newBuilder().setErrCode(e.getTrailers()
                    .get(ErrorStatusRuntimeException.ERROR_STATUS_KEY).getCodeValue()).build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getExCommissionFee(GetExCommissionFeeRequest request, StreamObserver<GetExCommissionFeeResponse> responseObserver) {
        GetExCommissionFeeResponse response = getFeeStup().getExCommissionFee(request);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateBrokerTradeMinFee(UpdateBrokerTradeMinFeeRequest request, StreamObserver<UpdateBrokerTradeMinFeeResponse> responseObserver) {
        UpdateBrokerTradeMinFeeResponse response;
        try{
            response = getFeeStup().updateBrokerTradeMinFee(request);
        }
        catch (StatusRuntimeException e){
            response = UpdateBrokerTradeMinFeeResponse.newBuilder().setErrCode(e.getTrailers()
                    .get(ErrorStatusRuntimeException.ERROR_STATUS_KEY).getCodeValue()).build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getBrokerTradeMinFee(GetBrokerTradeMinFeeRequest request, StreamObserver<GetBrokerTradeMinFeeResponse> responseObserver) {
        GetBrokerTradeMinFeeResponse response = getFeeStup().getBrokerTradeMinFee(request);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateMatchCommissionFee(UpdateMatchCommissionFeeRequest request, StreamObserver<UpdateMatchCommissionFeeResponse> responseObserver) {
        UpdateMatchCommissionFeeResponse response;
        try{
            response = getFeeStup().updateMatchCommissionFee(request);
        }
        catch (StatusRuntimeException e){
            response = UpdateMatchCommissionFeeResponse.newBuilder().setErrCode(e.getTrailers()
                    .get(ErrorStatusRuntimeException.ERROR_STATUS_KEY).getCodeValue()).build();
        }
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getMatchCommissionFee(GetMatchCommissionFeeRequest request, StreamObserver<GetMatchCommissionFeeResponse> responseObserver) {
        GetMatchCommissionFeeResponse response = getFeeStup().getMatchCommissionFee(request);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
