package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.DBTableConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface DBTableConfigMapper extends tk.mybatis.mapper.common.Mapper<DBTableConfig> {

}
