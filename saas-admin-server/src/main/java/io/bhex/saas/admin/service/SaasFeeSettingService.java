package io.bhex.saas.admin.service;

import io.bhex.saas.admin.model.BrokerSaasFeeRate;
import io.bhex.saas.admin.model.ExchangeSaasFeeRate;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/3 下午6:20
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

public interface SaasFeeSettingService {

    boolean createExchangeSaasFeeSetting(Long exchangeId, BigDecimal feeRate);

    boolean updateExchangeSaasFeeSetting(Long exchangeId, BigDecimal feeRate);

    ExchangeSaasFeeRate getLatestExchangeSaasFeeSetting(Long exchangeId);


    boolean createBrokerSaasFeeSetting(Long brokerId, BigDecimal feeRate);

    boolean updateBrokerSaasFeeSetting(Long brokerId, BigDecimal feeRate);

    BrokerSaasFeeRate getLatestBrokerSaasFeeSetting(Long brokerId);
}
