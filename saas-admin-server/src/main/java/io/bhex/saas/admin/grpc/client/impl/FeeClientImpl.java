package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.account.*;
import io.bhex.saas.admin.grpc.client.FeeClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/7/18 6:13 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class FeeClientImpl implements FeeClient {

    @Resource
    GrpcClientConfig grpcConfig;

    private FeeServiceGrpc.FeeServiceBlockingStub getFeeStup() {
        return grpcConfig.saasFeeServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public GetSaasCommissionFeeResponse getSaasCommissionFee(GetSaasCommissionFeeRequest request) {
        return getFeeStup().getSaasCommissionFee(request);
    }

    @Override
    public UpdateSaasCommissionFeeResponse updateSaasCommissionFee(UpdateSaasCommissionFeeRequest request) {
        return getFeeStup().updateSaasCommissionFee(request);
    }
}
