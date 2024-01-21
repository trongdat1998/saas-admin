package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeInstance;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface ExchangeInstanceMapper extends tk.mybatis.mapper.common.Mapper<ExchangeInstance> {

    @Select("select * from tb_exchange_instance where exchange_id = #{exchangeId}")
    ExchangeInstanceDetail getInstanceInfoByExchangeId(@Param("exchangeId") Long exchangeId);

}
