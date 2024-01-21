package io.bhex.saas.admin.dao;


import io.bhex.saas.admin.model.BrokerSaasFeeRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Mapper
@Component
public interface BrokerSaasFeeRateMapper  extends tk.mybatis.mapper.common.Mapper<BrokerSaasFeeRate> {

    @Update("update tb_broker_saas_fee_rate set action_time = #{actionTime}, fee_rate=#{feeRate}, update_at = #{updateAt} where id = #{id} and deleted = 0")
    int updateActionTime(@Param("id")Long id, @Param("actionTime")Date actionTime, @Param("feeRate")BigDecimal feeRate, @Param("updateAt")Timestamp updateAt);

    @Update("update tb_broker_saas_fee_rate set deleted = 1, update_at = #{updateAt} where id = #{id} and deleted = 0")
    int removeItem( @Param("id")Long id, @Param("updateAt")Timestamp updateAt);

    @Select("select * from tb_broker_saas_fee_rate where action_time <= #{today} and broker_id = #{brokerId} and deleted = 0 limit 1")
    BrokerSaasFeeRate getActivedSetting(@Param("brokerId")Long brokerId, @Param("today") Date today);

    @Select("select * from tb_broker_saas_fee_rate where broker_id = #{brokerId} and deleted = 0 order by action_time desc limit 1")
    BrokerSaasFeeRate getLatestSetting(@Param("brokerId")Long brokerId);
}
