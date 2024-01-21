package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.base.token.GetTokenListRequest;
import io.bhex.base.token.GetTokenRequest;
import io.bhex.base.token.GetTokensReply;
import io.bhex.base.token.GetTokensRequest;
import io.bhex.base.token.QueryTokenRequest;
import io.bhex.base.token.QueryTokensReply;
import io.bhex.base.token.TokenDetail;
import io.bhex.base.token.TokenList;
import io.bhex.base.token.TokenServiceGrpc;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2018/11/1 上午10:29
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class TokenProxyGrpcService extends TokenServiceGrpc.TokenServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    private TokenServiceGrpc.TokenServiceBlockingStub getStub(){
        return grpcConfig.tokenServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void getToken(GetTokenRequest request, StreamObserver<TokenDetail> responseObserver) {
        TokenDetail bhreply = getStub().getToken(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getTokenList(GetTokenListRequest request, StreamObserver<TokenList> responseObserver) {
        TokenList bhreply = getStub().getTokenList(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }



    @Override
    public void getTokens(GetTokensRequest request, StreamObserver<GetTokensReply> responseObserver) {
        GetTokensReply bhreply = getStub().getTokens(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryTokens(QueryTokenRequest request, StreamObserver<QueryTokensReply> responseObserver) {
        QueryTokensReply bhreply = getStub().queryTokens(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }
}
