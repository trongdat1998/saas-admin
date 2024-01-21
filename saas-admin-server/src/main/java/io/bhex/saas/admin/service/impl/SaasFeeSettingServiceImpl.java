package io.bhex.saas.admin.service.impl;

import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.saas.admin.dao.BrokerSaasFeeRateMapper;
import io.bhex.saas.admin.dao.ExchangeSaasFeeRateMapper;
import io.bhex.saas.admin.model.BrokerSaasFeeRate;
import io.bhex.saas.admin.model.ExchangeSaasFeeRate;
import io.bhex.saas.admin.service.SaasFeeSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
public class SaasFeeSettingServiceImpl implements SaasFeeSettingService {

    @Autowired
    private SnowflakeGenerator idGenerator;
    @Autowired
    private ExchangeSaasFeeRateMapper exchangeSaasFeeRateMapper;
    @Autowired
    private BrokerSaasFeeRateMapper brokerSaasFeeRateMapper;

    private Date getToday(){
        Date today = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return today;
    }

    @Override
    public boolean createExchangeSaasFeeSetting(Long exchangeId, BigDecimal feeRate) {
        ExchangeSaasFeeRate item = ExchangeSaasFeeRate.builder()
                .actionTime(getToday())
                .createAt(new Timestamp(System.currentTimeMillis()))
                .updateAt(new Timestamp(System.currentTimeMillis()))
                .deleted(0)
                .exchangeId(exchangeId)
                .feeRate(feeRate)
                .id(idGenerator.getLong())
                .build();
        return exchangeSaasFeeRateMapper.insert(item) == 1;
    }

    @Override
    public boolean updateExchangeSaasFeeSetting(Long exchangeId, BigDecimal feeRate) {

        Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //如果修改的是当前使用的，则新加一条; 如果有未生效的 直接修改
        ExchangeSaasFeeRate setting = exchangeSaasFeeRateMapper.getLatestSetting(exchangeId);
        log.info("exchangeID:{} change fee rate，from{}to{}", exchangeId, setting.getFeeRate(), feeRate);
        if(setting.getActionTime().compareTo(tomorrow) < 0){
            if(setting.getFeeRate().compareTo(feeRate) == 0){
                log.info("exchangeID:{} feerate no change, return directly.", exchangeId);
                return true;
            }
            ExchangeSaasFeeRate item = ExchangeSaasFeeRate.builder()
                    .actionTime(tomorrow)
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .updateAt(new Timestamp(System.currentTimeMillis()))
                    .deleted(0)
                    .exchangeId(exchangeId)
                    .feeRate(feeRate)
                    .id(idGenerator.getLong())
                    .build();
            return exchangeSaasFeeRateMapper.insert(item) == 1;
        }
        else{
            return 1 == exchangeSaasFeeRateMapper.updateActionTime(setting.getId(),
                    tomorrow, feeRate, new Timestamp(System.currentTimeMillis()));
        }



    }

    @Override
    public ExchangeSaasFeeRate getLatestExchangeSaasFeeSetting(Long exchangeId) {
        return exchangeSaasFeeRateMapper.getLatestSetting(exchangeId);
    }

    @Override
    public boolean createBrokerSaasFeeSetting(Long brokerId, BigDecimal feeRate) {
        BrokerSaasFeeRate item = BrokerSaasFeeRate.builder()
                .actionTime(getToday())
                .createAt(new Timestamp(System.currentTimeMillis()))
                .updateAt(new Timestamp(System.currentTimeMillis()))
                .deleted(0)
                .brokerId(brokerId)
                .feeRate(feeRate)
                .id(idGenerator.getLong())
                .build();
        return brokerSaasFeeRateMapper.insert(item) == 1;
    }

    @Override
    public boolean updateBrokerSaasFeeSetting(Long brokerId, BigDecimal feeRate) {
        Date tomorrow = Date.from(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());

        //如果修改的是当前使用的，则新加一条; 如果有未生效的 直接修改
        BrokerSaasFeeRate setting = brokerSaasFeeRateMapper.getLatestSetting(brokerId);
        if(setting.getActionTime().compareTo(tomorrow) < 0){
            BrokerSaasFeeRate item = BrokerSaasFeeRate.builder()
                    .actionTime(tomorrow)
                    .createAt(new Timestamp(System.currentTimeMillis()))
                    .updateAt(new Timestamp(System.currentTimeMillis()))
                    .deleted(0)
                    .brokerId(brokerId)
                    .feeRate(feeRate)
                    .id(idGenerator.getLong())
                    .build();
            return brokerSaasFeeRateMapper.insert(item) == 1;
        }
        else{
            return 1 == brokerSaasFeeRateMapper.updateActionTime(setting.getId(),
                    tomorrow, feeRate, new Timestamp(System.currentTimeMillis()));
        }
    }

    @Override
    public BrokerSaasFeeRate getLatestBrokerSaasFeeSetting(Long brokerId) {
        return brokerSaasFeeRateMapper.getLatestSetting(brokerId);
    }
}
