package io.bhex.saas.admin.service.impl;

import io.bhex.saas.admin.dao.BrokerInstanceMapper;
import io.bhex.saas.admin.model.BrokerInstance;
import io.bhex.saas.admin.service.BrokerInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 07/09/2018 2:34 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Service
public class BrokerInstanceServiceImpl implements BrokerInstanceService {

    @Autowired
    private BrokerInstanceMapper brokerInstanceMapper;

    @Override
    public List<BrokerInstance> listBrokerInstanceInfo() {
        return brokerInstanceMapper.listBrokerInstanceInfo();
    }

    @Override
    public BrokerInstance getInstanceInfoById(Long id) {
        return brokerInstanceMapper.getInstanceInfoById(id);
    }
}
