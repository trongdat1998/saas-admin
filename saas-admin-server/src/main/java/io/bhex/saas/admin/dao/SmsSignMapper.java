package io.bhex.saas.admin.dao;


import io.bhex.saas.admin.model.SmsSign;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@org.apache.ibatis.annotations.Mapper
public interface SmsSignMapper extends Mapper<SmsSign>{

    @Select("select * from tb_sms_sign where org_id= #{brokerId} and deleted = 0")
    SmsSign getByOrgId(@Param("brokerId") Long orgId);


    @Select("select * from tb_sms_sign where deleted = 0")
    List<SmsSign> selectSmsSigns(@Param("fromId") Long fromId,
                                 @Param("endId") Long endId,
                                 @Param("limit") Integer limit);

    @SelectProvider(type = Provider.class, method="selectSmsSignsByLastModify")
    List<SmsSign> selectSmsSignsByLastModify( @Param("brokerIds") List<Long> brokerIds, @Param("lastModify") Timestamp lastModify);

    class Provider{
        public String selectSmsSignsByLastModify(Map<String, Object> parameter) {
            List<Long> brokerIds = (List<Long>) parameter.get("brokerIds");
            List<String> strings = brokerIds.stream().map(id -> String.valueOf(id)).collect(Collectors.toList());
            String insql = String.join(",",strings);
            return new SQL() {
                {
                    SELECT("*").FROM("tb_sms_sign");
                    WHERE("deleted = 0")
                            .WHERE("updated_at > #{lastModify}")
                            .WHERE("org_id in (" + insql +")");
                }
            }.toString();
        }
    }

}
