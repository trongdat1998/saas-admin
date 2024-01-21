package io.bhex.saas.admin.grpc.client;

import io.bhex.saas.admin.controller.dto.IndexConfigDTO;
import io.bhex.saas.admin.controller.param.IndexConfigPO;

import java.util.List;

public interface IQuoteClient {

    void createResourcesWithNewExchange(Long exchangeId);

    List<IndexConfigDTO> configList(Integer page, Integer pageSize);

    void saveIndexConfig(IndexConfigPO indexConfigPO);
}
