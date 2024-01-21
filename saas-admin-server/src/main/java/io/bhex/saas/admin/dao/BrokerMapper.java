package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.Broker;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.dao
 * @Author: ming.xu
 * @CreateDate: 15/08/2018 11:19 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Mapper
@Component
public interface BrokerMapper extends tk.mybatis.mapper.common.Mapper<Broker> {

    String ALL_COLUMNS = " id,broker_id,instance_id,name,company,email,phone,host,earnest_address,contact,basic_info,remark,is_bind,enabled,register_option,created_at,updated_at,api_domain ";

    String INSERT_COLUMNS = " broker_id,instance_id,name,company,email,phone,host,earnest_address,contact,basic_info,remark,is_bind,enabled,register_option,created_at,updated_at ";

    String TABLE_NAME = " tb_broker ";

    @SelectProvider(type = BrokerSqlProvider.class, method = "countBroker")
    int countBroker(@Param("brokerName") String brokerName, @Param("brokerId") Long brokerId);

    @SelectProvider(type = BrokerSqlProvider.class, method = "queryBroker")
    List<Broker> queryBroker(@Param("start") int start, @Param("offset") int offset,
                                    @Param("brokerName") String brokerName, @Param("brokerId") Long brokerId);

    @Select("select " + ALL_COLUMNS + " from " + TABLE_NAME + " where broker_id = #{brokerId}")
    Broker getByBrokerId(@Param("brokerId") Long brokerId);

    @Select("select " + ALL_COLUMNS + " from " + TABLE_NAME + " where name = #{brokerName}")
    Broker getByBrokerName(@Param("brokerName") String brokerName);

    @Select("select ifnull(max(id),7001) from " + TABLE_NAME + " where id > 7000")
    Long getMaxId();

    @Select("select ifnull(max(id),6003) from " + TABLE_NAME + " where id < 7000")
    Long getMaxBhexId();

    @Select("select * from tb_broker where  enabled = 1")
    public List<Broker> getAllBrokers();
}
