package io.bhex.saas.admin.service.impl;

import io.bhex.broker.grpc.common_ini.CommonIni;
import io.bhex.saas.admin.grpc.client.CommonIniClient;
import io.bhex.saas.admin.service.CommonIniService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CommonIniServiceImpl implements CommonIniService {

    @Autowired
    CommonIniClient commonIniClient;

    @Override
    public boolean saveCommonIni(Long orgId, String iniName, String iniDesc, String iniValue, String language) {
        commonIniClient.saveCommonIni(orgId,iniName,iniDesc,iniValue,language);
        return true;
    }

    @Override
    public CommonIni getCommonIni(Long orgId, String iniName, String language) {
        return commonIniClient.getCommonIni(orgId, iniName, language);
    }
}
