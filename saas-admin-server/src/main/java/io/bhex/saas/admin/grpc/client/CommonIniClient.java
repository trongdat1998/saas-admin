package io.bhex.saas.admin.grpc.client;

import io.bhex.broker.grpc.common_ini.CommonIni;
import io.bhex.broker.grpc.common_ini.SaveCommonIniResponse;

public interface CommonIniClient {
    SaveCommonIniResponse saveCommonIni(Long orgId, String iniName, String iniDesc, String iniValue, String language);

    CommonIni getCommonIni(Long orgId, String iniName, String language);
}
