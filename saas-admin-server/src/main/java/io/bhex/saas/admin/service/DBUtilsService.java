package io.bhex.saas.admin.service;

import io.bhex.broker.grpc.admin.FetchOneResponse;
import io.bhex.saas.admin.model.DBTableConfig;

import java.util.List;
import java.util.Map;

public interface DBUtilsService {
    FetchOneResponse fetchOne(String namespace, String dbName, String tableName, String[] fieldNames, List<String[]> conditions);
    List<DBTableConfig> getTableConfigs();
}
