package io.bhex.saas.admin.grpc.server;

import io.bhex.base.admin.ContractDetail;
import io.bhex.base.admin.ListContractReply;
import io.bhex.base.admin.OrgType;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.token.QueryBrokerTokensRequest;
import io.bhex.base.token.QueryExchangeTokensRequest;
import io.bhex.base.token.QueryTokensReply;
import io.bhex.base.token.SaasTokenServiceGrpc;
import io.bhex.saas.admin.service.BrokerTokenService;
import io.bhex.saas.admin.service.OrgContractService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class SaasTokenGrpcService extends SaasTokenServiceGrpc.SaasTokenServiceImplBase {

    @Autowired
    private BrokerTokenService brokerTokenService;
    @Autowired
    private OrgContractService orgContractService;

    @Override
    public void queryExchangeTokens(QueryExchangeTokensRequest request, StreamObserver<QueryTokensReply> responseObserver) {

        QueryTokensReply reply = brokerTokenService.queryExchangeTokens(request.getExchangeId(),
                request.getTokenId(), request.getCurrent(), request.getPageSize(), request.getCategory());

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void queryBrokerTokens(QueryBrokerTokensRequest request, StreamObserver<QueryTokensReply> responseObserver) {
        List<Long> brokerIds = new ArrayList<>();
        brokerIds.add(request.getBrokerId());
        ListContractReply listContractReply = orgContractService.listAllOrgContractInfo(brokerIds, OrgType.Broker_Org);
        List<ContractDetail>  contractDetails = listContractReply.getContractDetailList();
        if (CollectionUtils.isEmpty(contractDetails)) {
            responseObserver.onNext(QueryTokensReply.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }
        List<Long> contractExchangeIds = contractDetails.stream()
                .map(contractDetail -> contractDetail.getExchangeId())
                .collect(Collectors.toList());
        QueryTokensReply reply = brokerTokenService.queryExchangeTokens(contractExchangeIds,
                request.getTokenId(), request.getCurrent(), request.getPageSize(), 0);

        responseObserver.onNext(reply);
        responseObserver.onCompleted();
     }
}
