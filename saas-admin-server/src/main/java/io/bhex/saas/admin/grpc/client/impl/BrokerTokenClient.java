package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.token.BrokerExchangeTokenServiceGrpc;
import io.bhex.base.token.GetBrokerTokensReply;
import io.bhex.base.token.GetBrokerTokensRequest;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class BrokerTokenClient {
    @Resource
    GrpcClientConfig grpcConfig;

    public BrokerExchangeTokenServiceGrpc.BrokerExchangeTokenServiceBlockingStub getStub() {
        return  grpcConfig.brokerExchangeTokenServiceBlockingStub();
    }

    public GetBrokerTokensReply queryBrokerTokens(GetBrokerTokensRequest request) {
        return getStub().getBrokerTokens(request);
    }

}
