package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface ExchangeInstanceDetailMapper extends tk.mybatis.mapper.common.Mapper<ExchangeInstanceDetail> {

    @Select("select * from tb_exchange_instance_detail where exchange_id = #{exchangeId}")
    ExchangeInstanceDetail getInstanceDetailByExchangeId(@Param("exchangeId") Long exchangeId);

    @Select("select * from tb_exchange_instance_detail where deleted = 0")
    List<ExchangeInstanceDetail> getAll();

    @Update("update tb_exchange_instance_detail set status = #{status} where  exchange_id = #{exchangeId}")
    int updateStatus(@Param("exchangeId") Long exchangeId, @Param("status") Integer status);
}
