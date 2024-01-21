package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.GetBrokerTradeMinFeeRequest;
import io.bhex.base.account.GetBrokerTradeMinFeeResponse;
import io.bhex.base.admin.common.BrokerTradeFeeRateReply;
import io.bhex.base.admin.common.BrokerTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.DeleteAccountFeeRateAdjustRequest;
import io.bhex.base.admin.common.DeleteAccountFeeRateAdjustResponse;
import io.bhex.base.admin.common.GetAccountFeeRateAdjustRequest;
import io.bhex.base.admin.common.GetAccountFeeRateAdjustResponse;
import io.bhex.base.admin.common.GetLatestBrokerTradeFeeRequest;
import io.bhex.base.admin.common.GetMinTradeFeeRateReply;
import io.bhex.base.admin.common.GetMinTradeFeeRateRequest;
import io.bhex.base.admin.common.UpdateAccountFeeRateAdjustRequest;
import io.bhex.base.admin.common.UpdateAccountFeeRateAdjustResponse;
import io.bhex.base.admin.common.UpdateBrokerTradeFeeReply;
import io.bhex.base.admin.common.UpdateBrokerTradeFeeRequest;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.GetSymbolRequest;
import io.bhex.base.token.SymbolDetail;
import io.bhex.base.token.SymbolServiceGrpc;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * @Description:
 * @Date: 2018/10/31 上午11:52
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class BrokerTradeFeeProxyGrpcService
        extends BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceImplBase {
    private static Long CATEGORY_COIN = 1L;

    @Resource
    GrpcClientConfig grpcConfig;

    private SymbolServiceGrpc.SymbolServiceBlockingStub getSymbolStub() {
        return grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private SymbolDetail getSymbol(String symbolId) {
        if (StringUtils.isEmpty(symbolId)) {
            return null;
        }
        GetSymbolRequest request = GetSymbolRequest.newBuilder()
                .setSymbolId(symbolId)
                .build();
        return getSymbolStub().getSymbol(request);
    }

    @Override
    public void updateBrokerTradeFee(UpdateBrokerTradeFeeRequest request,
                                     StreamObserver<UpdateBrokerTradeFeeReply> responseObserver) {
        SymbolDetail symbol = getSymbol(request.getSymbolId());
        if (Objects.isNull(symbol)) {
            UpdateBrokerTradeFeeReply reply = UpdateBrokerTradeFeeReply.newBuilder()
                    .setResult(false)
                    .setMessage("symbol.not.exist")
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        //只当设置币币费率时，才做最小值判断
        if (symbol.getCategory() == CATEGORY_COIN) {
            GetBrokerTradeMinFeeRequest minFeeRequest = GetBrokerTradeMinFeeRequest.newBuilder()
                    .setExchangeId(request.getExchangeId()).build();

            GetBrokerTradeMinFeeResponse bhExReply = grpcConfig.saasFeeServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME)
                    .getBrokerTradeMinFee(minFeeRequest);
            if (DecimalUtil.toBigDecimal(bhExReply.getMakerFeeRate())
                    .compareTo(new BigDecimal(request.getMakerFeeRate())) > 0) {
                UpdateBrokerTradeFeeReply reply = UpdateBrokerTradeFeeReply.newBuilder()
                        .setResult(false)
                        .setMessage("makerFeeRate.lower.than.exchange")
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            } else if (DecimalUtil.toBigDecimal(bhExReply.getTakerFeeRate())
                    .compareTo(new BigDecimal(request.getTakerFeeRate())) > 0) {
                UpdateBrokerTradeFeeReply reply = UpdateBrokerTradeFeeReply.newBuilder()
                        .setResult(false)
                        .setMessage("takerFeeRate.lower.than.exchange")
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            } else if (DecimalUtil.toBigDecimal(bhExReply.getMakerBonusRate())
                    .compareTo(new BigDecimal(request.getTakerRewardToMakerRate())) < 0) {
                UpdateBrokerTradeFeeReply reply = UpdateBrokerTradeFeeReply.newBuilder()
                        .setResult(false)
                        .setMessage("takerRewardToMakerRate.more.than.exchange")
                        .build();
                responseObserver.onNext(reply);
                responseObserver.onCompleted();
                return;
            }
        }


        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        UpdateBrokerTradeFeeReply bhreply = bhStub.updateBrokerTradeFee(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getLatestBrokerTradeFee(GetLatestBrokerTradeFeeRequest request,
                                        StreamObserver<BrokerTradeFeeRateReply> responseObserver) {

        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        BrokerTradeFeeRateReply bhreply = bhStub.getLatestBrokerTradeFee(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();

    }

    @Override
    public void getMinTradeFeeRate(GetMinTradeFeeRateRequest request,
                                   StreamObserver<GetMinTradeFeeRateReply> responseObserver) {
        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        GetMinTradeFeeRateReply bhreply = bhStub.getMinTradeFeeRate(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }


    @Override
    public void updateAccountFeeRateAdjust(UpdateAccountFeeRateAdjustRequest request, StreamObserver<UpdateAccountFeeRateAdjustResponse> responseObserver) {
        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        UpdateAccountFeeRateAdjustResponse bhreply = bhStub.updateAccountFeeRateAdjust(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();

//        responseObserver.onNext(UpdateAccountFeeRateAdjustResponse.newBuilder().build());
//        responseObserver.onCompleted();
    }

    @Override
    public void deleteAccountFeeRateAdjust(DeleteAccountFeeRateAdjustRequest request, StreamObserver<DeleteAccountFeeRateAdjustResponse> responseObserver) {
        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        DeleteAccountFeeRateAdjustResponse bhreply = bhStub.deleteAccountFeeRateAdjust(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();

//        responseObserver.onNext(DeleteAccountFeeRateAdjustResponse.newBuilder().build());
//        responseObserver.onCompleted();
    }

    @Override
    public void getAccountFeeRateAdjust(GetAccountFeeRateAdjustRequest request, StreamObserver<GetAccountFeeRateAdjustResponse> responseObserver) {
        BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub bhStub =
                grpcConfig.brokerTradeFeeSettingServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
        GetAccountFeeRateAdjustResponse bhreply = bhStub.getAccountFeeRateAdjust(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }
}
