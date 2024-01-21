package io.bhex.saas.admin.service.impl;

import io.bhex.saas.admin.controller.dto.StakingProductPermissionDTO;
import io.bhex.saas.admin.grpc.client.impl.BsStakingProductClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StakingProductService {

    @Autowired
    BsStakingProductClient bsStakingProductClient;

    public StakingProductPermissionDTO getBrokerProductPermission(Long brokerId) {
        return bsStakingProductClient.getBrokerProductPermission(brokerId);
    }

    public void setBrokerFixedProductPermission(Long brokerId, Integer status) {
        bsStakingProductClient.setBrokerFixedProductPermission(brokerId, status);
    }

    public void setBrokerFixedLockProductPermission(Long brokerId, Integer status) {
        bsStakingProductClient.setBrokerFixedLockProductPermission(brokerId, status);
    }

    public void setBrokerCurrentProductPermission(Long brokerId, Integer status) {
        bsStakingProductClient.setBrokerCurrentProductPermission(brokerId, status);
    }

}
