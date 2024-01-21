package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.broker.grpc.common_ini.*;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.grpc.client.CommonIniClient;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommonIniClientImpl implements CommonIniClient {

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;


    @Override
    public SaveCommonIniResponse saveCommonIni(Long orgId, String iniName, String iniDesc, String iniValue, String language) {
        try {
            CommonIniServiceGrpc.CommonIniServiceBlockingStub stub = CommonIniServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
            SaveCommonIniRequest request = SaveCommonIniRequest.newBuilder()
                    .setOrgId(orgId)
                    .setIniName(iniName)
                    .setIniDesc(iniDesc)
                    .setIniValue(iniValue)
                    .setLanguage(language)
                    .build();
            SaveCommonIniResponse response = stub.saveCommonIni(request);
            return response;
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }

    @Override
    public CommonIni getCommonIni(Long orgId, String iniName, String language) {
        try {
            CommonIniServiceGrpc.CommonIniServiceBlockingStub stub = CommonIniServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
            GetCommonIni2Request request = GetCommonIni2Request.newBuilder()
                    .setOrgId(orgId)
                    .setIniName(iniName)
                    .setLanguage(language)
                    .build();
            GetCommonIni2Response response = stub.getCommonIni2(request);
            return response.getInis();
        } catch (StatusRuntimeException e) {
            log.error("{}", e);
            throw new BizException(ErrorCode.ERROR);
        }
    }
}
