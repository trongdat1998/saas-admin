package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.quote.CommonResponse;
import io.bhex.base.quote.IndexConfigList;
import io.bhex.base.quote.NewExchangeRequest;
import io.bhex.base.quote.QuoteServiceGrpc;
import io.bhex.ex.quote.service.IGRpcIndexService;
import io.bhex.saas.admin.controller.dto.IndexConfigDTO;
import io.bhex.saas.admin.controller.param.IndexConfigPO;
import io.bhex.saas.admin.grpc.client.IQuoteClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuoteClientImpl implements IQuoteClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private IGRpcIndexService igRpcIndexService;

    @Override
    public void createResourcesWithNewExchange(Long exchangeId) {
        CommonResponse commonResponse = getQuoteBlockingStub().createQuoteResouceWithNewExchange(NewExchangeRequest.newBuilder()
            .setExchangeId(exchangeId)
            .build());
        if (commonResponse.getCode() != 0) {
            log.error("Create exchange [{}] resource error!", exchangeId);
        }
    }

    private QuoteServiceGrpc.QuoteServiceBlockingStub getQuoteBlockingStub() {
        return grpcConfig.quoteServiceBlockingStub(GrpcClientConfig.QUOTE_CHANNEL_NAME);
    }

    @Override
    public List<IndexConfigDTO> configList(Integer page, Integer pageSize) {
        IndexConfigList indexConfigList = igRpcIndexService.indexConfigList(page, pageSize);
        List<IndexConfigDTO> indexConfigDTOList = indexConfigList.getIndexConfigList()
            .stream()
            .map(IndexConfigDTO::parseProto)
            .collect(Collectors.toList());
        return indexConfigDTOList;
    }

    @Override
    public void saveIndexConfig(IndexConfigPO indexConfigPO) {
        igRpcIndexService.saveIndexConfig(indexConfigPO.getId(),
            indexConfigPO.getName(),
            indexConfigPO.getFormula(),
            indexConfigPO.getStrategy());
    }

}
