package io.bhex.saas.admin.service;

import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/10/8 下午2:51
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface OrgInstanceService {

    List<BrokerInstanceDetail> listBrokerInstances();

    BrokerInstanceDetail getBrokerInstanceDetailByBrokerId(Long brokerId);

    List<ExchangeInstanceDetail> listExchangeInstances();

    ExchangeInstanceDetail getExchangeInstanceDetailByBrokerId(Long exchangeId);

}
