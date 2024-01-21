package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.jdbc.SQL;
import tk.mybatis.mapper.code.ORDER;

import java.util.List;
import java.util.Map;

public class ExchangeInfoProvider {

    public String queryExchanges(Map<String, Object> parameter){

        String exchangeName = (String)parameter.get("exchangeName");
        Long exchangeId = (Long)parameter.get("exchangeId");


        return new SQL() {
            {
                SELECT("*");
                FROM("tb_exchange_info");
                WHERE("deleted = 0");
                if (!StringUtils.isEmpty(exchangeName)) {
                    WHERE("exchange_name = #{exchangeName}");
                }
                if (exchangeId != null && exchangeId > 0L) {
                    WHERE("exchange_id = #{exchangeId}");
                }
                ORDER_BY("created_at desc");
            }
        }.toString() + " LIMIT #{start},#{offset}";
    }

    public String countExchanges(Map<String, Object> parameter){
        String exchangeName = (String)parameter.get("exchangeName");
        Long exchangeId = (Long)parameter.get("exchangeId");


        return new SQL() {
            {
                SELECT("count(*)");
                FROM("tb_exchange_info");
                WHERE("deleted = 0");
                if (!StringUtils.isEmpty(exchangeName)) {
                    WHERE("exchange_name = #{exchangeName}");
                }
                if (exchangeId != null && exchangeId > 0L) {
                    WHERE("exchange_id = #{exchangeId}");
                }
            }
        }.toString();
    }
}
