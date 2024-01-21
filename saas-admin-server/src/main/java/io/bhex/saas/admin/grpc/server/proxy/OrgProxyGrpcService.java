package io.bhex.saas.admin.grpc.server.proxy;

import io.bhex.base.account.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.grpc.server.interceptor.GrpcServerLogInterceptor;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.server.proxy
 * @Author: ming.xu
 * @CreateDate: 02/11/2018 5:28 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@GrpcService(interceptors = GrpcServerLogInterceptor.class)
public class OrgProxyGrpcService extends OrgServiceGrpc.OrgServiceImplBase {

    @Autowired
    private BhOrgClient bhOrgClient;

    @Override
    public void configMatchTransfer(ConfigMatchTransferRequest request, StreamObserver<ConfigMatchTransferReply> responseObserver) {
        ConfigMatchTransferReply reply = bhOrgClient.configMatchTransfer(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrgByName(GetOrgByNameRequest request, StreamObserver<GetOrgByNameReply> responseObserver) {
        GetOrgByNameReply reply = bhOrgClient.getOrgByName(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getOrgById(GetOrgByIdRequest request, StreamObserver<GetOrgByIdReply> responseObserver) {
        GetOrgByIdReply reply = bhOrgClient.getOrgById(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void configBrokerExchangeContract(ConfigBrokerExchangeContractRequest request, StreamObserver<ConfigBrokerExchangeContractReply> responseObserver) {
        ConfigBrokerExchangeContractReply reply = bhOrgClient.configBrokerExchangeContract(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getEarnestAddress(GetEarnestAddressRequest request, StreamObserver<GetEarnestAddressReply> responseObserver) {
        GetEarnestAddressReply reply = bhOrgClient.getEarnestAddress(request);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
