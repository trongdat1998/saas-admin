package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.account.*;
import io.bhex.base.token.GetTokenIdsRequest;
import io.bhex.base.token.TokenList;
import io.bhex.base.token.TokenServiceGrpc;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-15 15:55
 */
@Slf4j
@Service
public class BrokerDepositClient {

    @Resource
    GrpcClientConfig grpcConfig;

    private DepositServiceGrpc.DepositServiceBlockingStub depositServiceBlockingStub() {
        return grpcConfig.saasDepositServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    public TokenServiceGrpc.TokenServiceBlockingStub getTokenDetailStub() {
        return grpcConfig.tokenServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    public GetDepositsForAdminReply queryOrgDepositOrder(GetDepositsForAdminRequest request) {
        DepositServiceGrpc.DepositServiceBlockingStub stub = depositServiceBlockingStub();
        try {
            return stub.getDepositsForAdmin(request);
        } catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }

    public TokenList queryQuoteTokens(GetTokenIdsRequest request) {
        TokenServiceGrpc.TokenServiceBlockingStub stub = getTokenDetailStub();
        try {
            return stub.getTokenListByIds(request);
        } catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }

    public SaasReceiptReply saasReceipt(SaasReceiptRequest request){
        DepositServiceGrpc.DepositServiceBlockingStub stub = depositServiceBlockingStub();
        try{
            return stub.saasReceipt(request);
        }catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }
    public ForAddressReply forAddress(ForAddressRequest request){
        DepositServiceGrpc.DepositServiceBlockingStub stub = depositServiceBlockingStub();
        try{
            return stub.forAddress(request);
        }catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }
}
