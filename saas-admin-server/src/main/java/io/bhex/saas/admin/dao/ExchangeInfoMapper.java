package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeInfo;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface ExchangeInfoMapper  extends tk.mybatis.mapper.common.Mapper<ExchangeInfo> {

    @Select("select * from tb_exchange_info where deleted=0 limit #{start},#{offset}")
    public List<ExchangeInfo> getExchanges(@Param("start") int start, @Param("offset") int offset);

    @SelectProvider(type=ExchangeInfoProvider.class, method = "queryExchanges")
    public List<ExchangeInfo> queryExchanges(@Param("start") int start, @Param("offset") int offset,
                                             @Param("exchangeName") String exchangeName,@Param("exchangeId") Long exchangeId);

    @SelectProvider(type=ExchangeInfoProvider.class, method = "countExchanges")
    public int countExchanges(@Param("exchangeName") String exchangeName,@Param("exchangeId") Long exchangeId);


    @Update("update tb_exchange_info set status = #{newStatus},updated_at=#{now} where id = #{id} and deleted=0 and status = #{oldStatus}")
    public int updateStatus(@Param("id") Long id,@Param("oldStatus") Integer oldStatus,
                            @Param("newStatus") Integer newStatus,@Param("now") Long now);

    @Select("select * from tb_exchange_info where deleted=0 and id = #{id}")
    public ExchangeInfo getExchangeInfoById(@Param("id") Long id);

    @Select("select * from tb_exchange_info where deleted=0 and exchange_id = #{exchangeId}")
    public ExchangeInfo getExchangeInfoByExchangeId(@Param("exchangeId") Long exchangeId);

    @Select("select * from tb_exchange_info where deleted=0 and exchange_name = #{exchangeName} limit 1")
    public ExchangeInfo getExchangeInfoByExchangeName(@Param("exchangeName") String exchangeName);

    @Select("select count(*) from tb_exchange_info where deleted=0 and email = #{email} limit 1")
    public int countByEmail(@Param("email") String email);

    @Select("select ifnull(max(id),601) from tb_exchange_info where id > 600")
    public Long getMaxId();

    @Select("select * from tb_exchange_info where deleted=0 and status = 1")
    public List<ExchangeInfo> getAllExchanges();

}
