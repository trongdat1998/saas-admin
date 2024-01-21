package io.bhex.saas.admin.dao;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.dao
 * @Author: ming.xu
 * @CreateDate: 05/09/2018 3:07 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public class BrokerSqlProvider {

    public String queryBroker(Map<String, Object> parameter){
        String brokerName = (String)parameter.get("brokerName");
        Long brokerId = (Long)parameter.get("brokerId");
        return new SQL() {
            {
                SELECT(BrokerMapper.ALL_COLUMNS);
                FROM(BrokerMapper.TABLE_NAME);
                if (!StringUtils.isEmpty(brokerName)) {
                    WHERE("name = #{brokerName}");
                }
                if (brokerId != null && brokerId > 0L) {
                    WHERE("broker_id = #{brokerId}");
                }
            }
        }.toString() + " LIMIT #{start},#{offset}";
    }

    public String countBroker(Map<String, Object> parameter){
        String brokerName = (String)parameter.get("brokerName");
        Long brokerId = (Long)parameter.get("brokerId");

        return new SQL() {
            {
                SELECT("count(*)");
                FROM(BrokerMapper.TABLE_NAME);
                if (!StringUtils.isEmpty(brokerName)) {
                    WHERE("name = #{brokerName}");
                }
                if (brokerId != null && brokerId > 0L) {
                    WHERE("broker_id = #{brokerId}");
                }
            }
        }.toString();
    }
}
