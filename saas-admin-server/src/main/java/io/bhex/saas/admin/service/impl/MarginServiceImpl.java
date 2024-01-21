package io.bhex.saas.admin.service.impl;

import io.bhex.base.margin.cross.FundingCrossReply;
import io.bhex.base.margin.cross.FundingCrossRequest;
import io.bhex.base.margin.cross.UpdateFundingCrossReply;
import io.bhex.base.margin.cross.UpdateFundingCrossRequest;
import io.bhex.base.proto.BaseRequest;
import io.bhex.saas.admin.controller.dto.MarginFundingCrossDTO;
import io.bhex.saas.admin.grpc.client.MarginClient;
import io.bhex.saas.admin.service.MarginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 16:43
 */
@Slf4j
@Service
public class MarginServiceImpl implements MarginService {
    @Autowired
    MarginClient marginClient;

    @Override
    public UpdateFundingCrossReply updateFundingCross(Long orgId, Long accountId) {
        UpdateFundingCrossRequest request = UpdateFundingCrossRequest.newBuilder()
                .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(orgId).build())
                .addAllAccountIds(Arrays.asList(accountId))
                .setTokenId("BTC")
                .build();

        return marginClient.updateFundingCross(request);
    }

    @Override
    public List<MarginFundingCrossDTO> queryFundingCross(Long orgId) {
        FundingCrossRequest request = FundingCrossRequest.newBuilder()
                .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(orgId).build())
                .build();
        FundingCrossReply reply = marginClient.funingCross(request);
        return reply.getFundingCrossList().stream()
                .map(fundingCross -> MarginFundingCrossDTO.builder()
                        .orgId(fundingCross.getOrgId())
                        .tokenId(fundingCross.getTokenId())
                        .accounts(fundingCross.getAccounts())
                        .build())
                .collect(Collectors.toList());
    }
}
