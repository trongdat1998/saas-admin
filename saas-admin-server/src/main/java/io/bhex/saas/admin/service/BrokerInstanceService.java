package io.bhex.saas.admin.service;

import io.bhex.saas.admin.model.BrokerInstance;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service
 * @Author: ming.xu
 * @CreateDate: 07/09/2018 2:34 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface BrokerInstanceService {

    List<BrokerInstance> listBrokerInstanceInfo();

    BrokerInstance getInstanceInfoById(Long id);
}
