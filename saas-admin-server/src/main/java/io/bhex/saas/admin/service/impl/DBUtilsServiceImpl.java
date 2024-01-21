package io.bhex.saas.admin.service.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.bhex.broker.grpc.admin.Condition;
import io.bhex.broker.grpc.admin.FetchOneRequest;
import io.bhex.broker.grpc.admin.FetchOneResponse;
import io.bhex.saas.admin.dao.DBTableConfigMapper;
import io.bhex.saas.admin.grpc.client.BrokerDBUtilsClient;
import io.bhex.saas.admin.model.DBTableConfig;
import io.bhex.saas.admin.service.DBUtilsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DBUtilsServiceImpl implements DBUtilsService {

    @Resource
    BrokerDBUtilsClient brokerDBUtilsClient;

    @Resource
    DBTableConfigMapper dbTableConfigMapper;

    static Cache<String, DBTableConfig> dbTableConfigCache = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();

    private DBTableConfig getConfig(String dbName, String tableName) {
        try {
            DBTableConfig dbTableConfig = dbTableConfigCache.get(dbName + "#" + tableName, () -> {
                Example example = Example.builder(DBTableConfig.class).build();
                example.createCriteria().andEqualTo("dbName", dbName).andEqualTo("tableName", tableName);
                return dbTableConfigMapper.selectOneByExample(example);
            });
            return dbTableConfig;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public FetchOneResponse fetchOne(String namespace, String dbName, String tableName, String[] fieldNames, List<String[]> conditions) {
        DBTableConfig dbTableConfig = getConfig(dbName, tableName);
        if (dbTableConfig == null) {
            return FetchOneResponse.newBuilder().setResult("no permission").setRet(1).build();
        }
        Set<String> condFields = conditions.stream().map(arr -> arr[0].toLowerCase()).collect(Collectors.toSet());
        //判断当前查询条件是否是配置过的索引
        boolean containIndex = false;
        for (String fields : dbTableConfig.getTableIndexs().split(";")) {
            String[] arr = fields.split(",");
            Set<String> condFields2 = new HashSet<>();
            for (String field : arr) {
                if (condFields.contains(field)) {
                    containIndex = true;
                    break;
                }
            }
            if (containIndex) {
                break;
            }
        }
        if (!containIndex) {
            return FetchOneResponse.newBuilder().setResult("use index in " + dbTableConfig.getTableIndexs()).setRet(1).build();
        }
        FetchOneRequest request = FetchOneRequest.newBuilder()
                .addAllConditions(conditions.stream()
                        .map(arr -> Condition.newBuilder()
                                .setName(arr[0])
                                .setCondition(arr[1])
                                .setValue(arr[2])
                                .build())
                        .collect(Collectors.toList()))
                .setTableName(tableName)
                .build();
        if (dbName.equalsIgnoreCase("broker")) {
            return brokerDBUtilsClient.fetchOneBroker(namespace, request);
        } else if (dbName.equalsIgnoreCase("statistics")) {
            return brokerDBUtilsClient.fetchOneStatistics(namespace, request);
        } else {
            return FetchOneResponse.newBuilder().setRet(1).setResult("unknow dbname").build();
        }
    }

    @Override
    public List<DBTableConfig> getTableConfigs() {
        return dbTableConfigMapper.selectAll();
    }


}
