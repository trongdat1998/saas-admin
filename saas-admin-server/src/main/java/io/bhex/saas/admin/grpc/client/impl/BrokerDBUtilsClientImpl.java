package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.broker.grpc.admin.AdminBrokerDBToolsServiceGrpc;
import io.bhex.broker.grpc.admin.FetchOneRequest;
import io.bhex.broker.grpc.admin.FetchOneResponse;
import io.bhex.saas.admin.grpc.client.BrokerDBUtilsClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class BrokerDBUtilsClientImpl implements BrokerDBUtilsClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Override
    public FetchOneResponse fetchOneBroker(String namespace, FetchOneRequest request) {
        try {
            AdminBrokerDBToolsServiceGrpc.AdminBrokerDBToolsServiceBlockingStub stub = grpcConfig.adminBrokerDBToolsServiceBlockingStub();
            return stub.fetchOneBroker(request);
        } catch (Exception e) {
            log.info("fetchOneBroker:" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public FetchOneResponse fetchOneStatistics(String namespace, FetchOneRequest request) {
        try {
            AdminBrokerDBToolsServiceGrpc.AdminBrokerDBToolsServiceBlockingStub stub = grpcConfig.adminBrokerDBToolsServiceBlockingStub();
            return stub.fetchOneStatistics(request);
        } catch (Exception e) {
            log.info("fetchOneBroker:" + e.getMessage(), e);
            return null;
        }
    }
}
