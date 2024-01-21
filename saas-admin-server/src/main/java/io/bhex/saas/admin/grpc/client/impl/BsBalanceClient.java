package io.bhex.saas.admin.grpc.client.impl;

import com.google.common.collect.Lists;
import io.bhex.base.account.BusinessSubject;
import io.bhex.broker.grpc.common.Header;
import io.bhex.broker.grpc.statistics.QueryOrgBalanceFlowRequest;
import io.bhex.broker.grpc.statistics.QueryOrgBalanceFlowResponse;
import io.bhex.broker.grpc.statistics.StatisticsServiceGrpc;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.controller.dto.BalanceFlowDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BsBalanceClient {

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;


    public List<BalanceFlowDTO> getBalanceFlows(Long orgId, Long accountId, BusinessSubject businessSubject, String tokenId, Long fromId, int limit) {
        List<Integer> subjects = new ArrayList<>();
        if (businessSubject != null) {
            subjects = Lists.newArrayList(businessSubject.getNumber());
        }

        QueryOrgBalanceFlowRequest.Builder builder = QueryOrgBalanceFlowRequest.newBuilder()
                .setHeader(Header.newBuilder().setOrgId(orgId).build())
                .setAccountId(accountId)
                .setFromId(fromId)
                //.setLastId(fromId)
                .setLimit(limit);
        if (!CollectionUtils.isEmpty(subjects)) {
            builder.addAllBusinessSubject(subjects);
        }
        if (!StringUtils.isEmpty(tokenId)) {
            builder.setTokenId(tokenId);
        }

        StatisticsServiceGrpc.StatisticsServiceBlockingStub stub = StatisticsServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
        QueryOrgBalanceFlowResponse reply = stub.queryOrgBalanceFlow(builder.build());
        List<QueryOrgBalanceFlowResponse.BalanceFlow> details = reply.getBalanceFlowList();


        List<BalanceFlowDTO> resList = details.stream().map(detail -> {
            BalanceFlowDTO res = new BalanceFlowDTO();
            res.setUserId(Long.parseLong(detail.getUserId()));
            res.setTokenId(detail.getTokenId());
            res.setCreated(detail.getCreatedTime());
            res.setTotal(new BigDecimal(detail.getTotal()));
            res.setChanged(new BigDecimal(detail.getChanged()));
            res.setBusinessSubject(detail.getBusinessSubject());
            res.setBalanceFlowId(detail.getBalanceFlowId());
            return res;
        }).collect(Collectors.toList());
        return resList;
    }

}


