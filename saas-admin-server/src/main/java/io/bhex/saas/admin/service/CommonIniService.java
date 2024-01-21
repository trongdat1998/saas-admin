package io.bhex.saas.admin.service;

import io.bhex.broker.grpc.common_ini.CommonIni;

public interface CommonIniService {
    boolean saveCommonIni(Long orgId, String iniName, String iniDesc, String iniValue, String language);

    CommonIni getCommonIni(Long orgId, String iniName, String language);
}
