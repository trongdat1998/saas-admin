package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.margin.*;
import io.bhex.base.margin.cross.*;
import io.bhex.saas.admin.grpc.client.MarginClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 16:46
 */
@Slf4j
@Service
public class MarginClientImpl implements MarginClient {

    @Resource
    GrpcClientConfig grpcConfig;

    private MarginCrossServiceGrpc.MarginCrossServiceBlockingStub getMarginStub() {
        return grpcConfig.marginCrossServiceBlockingStub();
    }

    private MarginConfigServiceGrpc.MarginConfigServiceBlockingStub getMarginConfigServiceStub(){
        return grpcConfig.marginConfigServiceBlockingStub();
    }


    @Override
    public UpdateFundingCrossReply updateFundingCross(UpdateFundingCrossRequest request) {

        return getMarginStub().updateFundingCross(request);
    }

    @Override
    public FundingCrossReply funingCross(FundingCrossRequest request) {

        return getMarginStub().getFundingCross(request);
    }

    @Override
    public QueryMarginSymbolReply queryMarginSymbol(QueryMarginSymbolRequest request) {
        return getMarginConfigServiceStub().queryMarginSymbol(request);
    }

    @Override
    public SetSymbolConfigReply updateSymbolInfo(SetSymbolConfigRequest request) {
        return getMarginConfigServiceStub().updateSymbolInfo(request);
    }

}
