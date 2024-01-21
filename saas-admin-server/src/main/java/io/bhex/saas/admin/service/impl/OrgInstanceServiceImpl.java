package io.bhex.saas.admin.service.impl;

import io.bhex.base.wallet.Exchange;
import io.bhex.saas.admin.dao.BrokerInstanceDetailMapper;
import io.bhex.saas.admin.dao.BrokerInstanceMapper;
import io.bhex.saas.admin.dao.ExchangeInstanceDetailMapper;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.service.OrgInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/10/8 下午2:53
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public class OrgInstanceServiceImpl implements OrgInstanceService {

    @Autowired
    private ExchangeInstanceDetailMapper exchangeInstanceMapper;
    @Autowired
    private BrokerInstanceDetailMapper brokerInstanceDetailMapper;

    @Override
    public List<BrokerInstanceDetail> listBrokerInstances() {
        return brokerInstanceDetailMapper.getAll();
    }

    @Override
    public BrokerInstanceDetail getBrokerInstanceDetailByBrokerId(Long brokerId) {
        return brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
    }

    @Override
    public List<ExchangeInstanceDetail> listExchangeInstances() {
        return exchangeInstanceMapper.getAll();
    }

    @Override
    public ExchangeInstanceDetail getExchangeInstanceDetailByBrokerId(Long exchangeId) {
        return exchangeInstanceMapper.getInstanceDetailByExchangeId(exchangeId);
    }
}
