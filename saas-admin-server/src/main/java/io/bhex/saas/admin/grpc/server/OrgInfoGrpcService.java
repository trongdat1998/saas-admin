package io.bhex.saas.admin.grpc.server;

import io.bhex.base.admin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.ExchangeInfo;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.server
 * @Author: ming.xu
 * @CreateDate: 04/11/2018 11:08 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@GrpcService
public class OrgInfoGrpcService extends AdminOrgInfoServiceGrpc.AdminOrgInfoServiceImplBase {

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private ExchangeInfoService exchangeInfoService;

    @Override
    public void queryBrokerOrg(QueryBrokerOrgRequest request, StreamObserver<QueryBrokerOrgReplay> responseObserver) {
        Broker broker = null;
        QueryBrokerOrgReplay.Builder builder = QueryBrokerOrgReplay.newBuilder();
        if (request.getBrokerId() != 0L) {
            broker = brokerService.getBrokerById(request.getBrokerId());
        } else if (StringUtils.isNotEmpty(request.getBrokerName())) {
            broker = brokerService.getBrokerByBrokerName(request.getBrokerName());
        }
        if (null != broker) {
            builder.setBrokerId(broker.getBrokerId());
            builder.setBrokerName(broker.getName());
            builder.setCompanyName(broker.getCompany());
            builder.setRegisterOption(broker.getRegisterOption() != null ? broker.getRegisterOption() : 0);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void queryExchangeOrg(QueryExchangeOrgRequest request, StreamObserver<QueryExchangeOrgReplay> responseObserver) {
        ExchangeInfo exchangeInfo = null;
        QueryExchangeOrgReplay.Builder builder = QueryExchangeOrgReplay.newBuilder();
        if (request.getExchangeId() != 0L) {
            exchangeInfo = exchangeInfoService.getExchangeInfoById(request.getExchangeId());
        } else if (StringUtils.isNotEmpty(request.getExchangeName())) {
            exchangeInfo = exchangeInfoService.getExchangeInfoByExchangeName(request.getExchangeName());
        }
        if (null != exchangeInfo) {
            builder.setExchangeId(exchangeInfo.getExchangeId());
            builder.setExchangeName(exchangeInfo.getExchangeName());
            builder.setCompanyName(exchangeInfo.getCompany());
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
