package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.BrokerInstance;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.dao
 * @Author: ming.xu
 * @CreateDate: 07/09/2018 2:26 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@org.apache.ibatis.annotations.Mapper
@Component
public interface BrokerInstanceMapper extends Mapper<BrokerInstance> {

    String ALL_COLUMNS = " * ";

    String TABLE_NAME = " tb_broker_instance ";

    @Select("select count(*) from " + TABLE_NAME)
    public int countBrokerInstance();

    @Select("select " + ALL_COLUMNS + " from " + TABLE_NAME)
    List<BrokerInstance> listBrokerInstanceInfo();

    @Select("select " + ALL_COLUMNS + " from " + TABLE_NAME + " where id = #{id}")
    BrokerInstance getInstanceInfoById(@Param("id") Long id);
}
