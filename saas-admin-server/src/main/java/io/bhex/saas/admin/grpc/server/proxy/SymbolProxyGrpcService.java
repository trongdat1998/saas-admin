package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.base.token.*;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.model.OrgContract;
import io.bhex.saas.admin.service.BrokerSymbolService;
import io.bhex.saas.admin.service.OrgContractService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;

/**
 * @Description:
 * @Date: 2018/11/1 上午10:59
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class SymbolProxyGrpcService extends SymbolServiceGrpc.SymbolServiceImplBase {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerSymbolService exchangeSymbolService;

    @Autowired
    private OrgContractService orgContractService;

    private SymbolServiceGrpc.SymbolServiceBlockingStub getStub(){
        return grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public void getSymbol(GetSymbolRequest request, StreamObserver<SymbolDetail> responseObserver) {
        SymbolDetail bhreply = getStub().getSymbol(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }



    @Override
    public void getSymbolList(GetSymbolListRequest request, StreamObserver<SymbolList> responseObserver){
        SymbolList bhreply = getStub().getSymbolList(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void getExchangeSymbols(GetExchangeSymbolsRequest request, StreamObserver<GetExchangeSymbolsReply> responseObserver) {
        GetExchangeSymbolsReply bhreply = getStub().getExchangeSymbols(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryExchangeSymbols(QueryExchangeSymbolsRequest request, StreamObserver<QueryExchangeSymbolsReply> responseObserver) {
        QueryExchangeSymbolsReply reply = exchangeSymbolService.queryExchangeSymbols(request.getExchangeId(),
                request.getSymbolId(), request.getCurrent(), request.getPageSize(), request.getMyAgentSymbol(), request.getCategory());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }



    @Override
    public void allowExchangeSymbolTrade(AllowExchangeSymbolTradeRequest request,
                                         StreamObserver<AllowExchangeSymbolTradeReply> responseObserver) {
        AllowExchangeSymbolTradeReply bhreply = getStub().allowExchangeSymbolTrade(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void publishExchangeSymbol(PublishExchangeSymbolRequest request,
                                      StreamObserver<PublishExchangeSymbolReply> responseObserver) {
        PublishExchangeSymbolReply bhreply = getStub().publishExchangeSymbol(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryBrokerExchangeSymbols(QueryBrokerExchangeSymbolsRequest request,
                                           StreamObserver<QueryBrokerExchangeSymbolsReply> responseObserver) {
        OrgContract orgContract = orgContractService.getOrgContractInContract(request.getExchangeId(), request.getBrokerId());
        log.info("request:{} orgContract:{}", request, orgContract);
        if(orgContract == null){
            responseObserver.onNext(QueryBrokerExchangeSymbolsReply.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }

        QueryBrokerExchangeSymbolsReply reply = exchangeSymbolService.queryBrokerExchangeSymbols(request.getExchangeId(),
                request.getCurrent(), request.getPageSize(), request.getCategory());
        log.info("queryBrokerExchangeSymbols request:{} response:{}", request, reply);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryOwenFutures(QueryOwenFuturesRequest request, StreamObserver<QueryOwenFuturesReply> responseObserver) {
        QueryOwenFuturesReply bhreply = getStub().queryOwenFutures(request);
        responseObserver.onNext(bhreply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryAgentFutures(QueryAgentFuturesRequest request, StreamObserver<QueryAgentFuturesReply> responseObserver) {
        QueryAgentFuturesReply reply = exchangeSymbolService.queryAgentFutures(request.getExchangeId(), request.getSymbolId(), request.getCurrent(), request.getPageSize());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
