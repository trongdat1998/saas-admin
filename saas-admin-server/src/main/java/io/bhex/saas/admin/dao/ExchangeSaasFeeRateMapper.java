package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.ExchangeSaasFeeRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Mapper
@Component
public interface ExchangeSaasFeeRateMapper   extends tk.mybatis.mapper.common.Mapper<ExchangeSaasFeeRate>{

    @Update("update tb_exchange_saas_fee_rate set action_time = #{actionTime}, fee_rate=#{feeRate}, update_at = #{updateAt} where id = #{id} and deleted = 0")
    int updateActionTime(@Param("id")Long id, @Param("actionTime")Date actionTime, @Param("feeRate")BigDecimal feeRate, @Param("updateAt")Timestamp updateAt);

    @Update("update tb_exchange_saas_fee_rate set deleted = 1, update_at = #{updateAt} where id = #{id} and deleted = 0")
    int removeItem( @Param("id")Long id, @Param("updateAt")Timestamp updateAt);

    @Select("select * from tb_exchange_saas_fee_rate where action_time <= #{today} and exchange_id = #{exchangeId} and deleted = 0 limit 1")
    ExchangeSaasFeeRate getActivedSetting(@Param("exchangeId")Long exchangeId, @Param("today") Date today);

    @Select("select * from tb_exchange_saas_fee_rate where exchange_id = #{exchangeId} and deleted = 0 order by action_time desc limit 1")
    ExchangeSaasFeeRate getLatestSetting(@Param("exchangeId")Long exchangeId);
}
