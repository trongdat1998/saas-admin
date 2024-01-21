package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.admin.common.BrokerTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.GetMinTradeFeeRateReply;
import io.bhex.base.admin.common.GetMinTradeFeeRateRequest;
import io.bhex.base.exadmin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @Description:
 * @Date: 2018/10/31 上午11:52
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class ExchangeTradeFeeProxyGrpcSerivce
        extends ExchangeTradeFeeSettingServiceGrpc.ExchangeTradeFeeSettingServiceImplBase{

    @Resource
    GrpcClientConfig grpcConfig;


    @Override
    public void updateExchangeTradeFee(UpdateExchangeTradeFeeRequest request,
                                       StreamObserver<UpdateExchangeTradeFeeReply> responseObserver) {
        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhBrokerStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        GetMinTradeFeeRateRequest.Builder builder = GetMinTradeFeeRateRequest.newBuilder()
                .setExchangeId(request.getExchangeId())
                .setSymoblId(request.getSymbolId());
        GetMinTradeFeeRateReply bhBrokerReply = bhBrokerStub.getMinTradeFeeRate(builder.build());
        BigDecimal minMakerFeeRate = new BigDecimal(bhBrokerReply.getMinMakerFeeRate());
        if(minMakerFeeRate.compareTo(BigDecimal.ZERO) > 0 &&
                DecimalUtil.toBigDecimal(request.getMakerFeeRate()).compareTo(minMakerFeeRate) > 0){
            UpdateExchangeTradeFeeReply reply = UpdateExchangeTradeFeeReply.newBuilder()
                    .setResult(false)
                    .setMessage("maker.fee.greater.than.broker")
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            return;
        }

        BigDecimal minTakerFeeRate = new BigDecimal(bhBrokerReply.getMinTakerFeeRate());
        if(minTakerFeeRate.compareTo(BigDecimal.ZERO) > 0 &&
                DecimalUtil.toBigDecimal(request.getTakerFeeRate()).compareTo(minTakerFeeRate) > 0){
            UpdateExchangeTradeFeeReply reply = UpdateExchangeTradeFeeReply.newBuilder()
                    .setResult(false)
                    .setMessage("taker.fee.greater.than.broker")
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
            return;
        }

        ExchangeTradeFeeSettingServiceGrpc.ExchangeTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.exchangeTradeFeeSettingServiceBlockingStub();
        UpdateExchangeTradeFeeReply bhreply = bhStub.updateExchangeTradeFee(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getLatestExchangeTradeFee(GetLatestExchangeTradeFeeRequest request,
                                          StreamObserver<ExchangeTradeFeeRateReply> responseObserver) {
        ExchangeTradeFeeSettingServiceGrpc.ExchangeTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.exchangeTradeFeeSettingServiceBlockingStub();
        ExchangeTradeFeeRateReply bhreply = bhStub.getLatestExchangeTradeFee(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }
}
