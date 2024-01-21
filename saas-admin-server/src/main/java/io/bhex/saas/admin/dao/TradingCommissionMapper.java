package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.TradingCommission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;


@Mapper
@Component
public interface TradingCommissionMapper extends tk.mybatis.mapper.common.Mapper<TradingCommission> {

    @Select("SELECT exchange_id, exchange_saas_fee, clear_time, fee_token_id, sum( trading_amount ) trading_amount, " +
            "sum( total_fee ) total_fee, sum( sys_fee ) sys_fee,  sum( exchange_saas_fee ) exchange_saas_fee FROM   tb_trading_commission " +
            "WHERE   clear_day = #{clearDay}  " +
            "GROUP BY   exchange_id,   fee_token_id,   clear_day")
    List<TradingCommission> selectSaasCommissions(@Param("clearDay") String clearDay);

    @Select("SELECT broker_id, clear_time, fee_token_id, sum( trading_amount ) trading_amount, " +
            "sum( total_fee ) total_fee, sum( sys_fee ) sys_fee,  sum( exchange_saas_fee ) exchange_saas_fee,sum( broker_saas_fee )  broker_saas_fee FROM   tb_trading_commission " +
            "WHERE   clear_day = #{clearDay} and exchange_id=#{exchangeId} " +
            "GROUP BY   broker_id")
    List<TradingCommission> selectExCommissionDetails(@Param("clearDay") String clearDay);
}
