package io.bhex.saas.admin.grpc.client.impl;


import io.bhex.broker.grpc.staking.*;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.controller.dto.StakingProductPermissionDTO;
import io.grpc.Deadline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BsStakingProductClient {

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    private AdminStakingProductServiceGrpc.AdminStakingProductServiceBlockingStub getStakingStub(long orgId) {
        return AdminStakingProductServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }

    public StakingProductPermissionDTO getBrokerProductPermission(Long brokerId) {

        AdminGetBrokerProductPermissionRequest request = AdminGetBrokerProductPermissionRequest.newBuilder().setBrokerId(brokerId).build();
        AdminGetBrokerProductPermissionReply reply = getStakingStub(brokerId).getBrokerProductPermission(request);

        StakingProductPermissionDTO dto = new StakingProductPermissionDTO();
        dto.setAllowFixed(reply.getAllowFixed());
        dto.setAllowFixedLock(reply.getAllowFixedLock());
        dto.setAllowCurrent(reply.getAllowCurrent());
        dto.setBrokerId(reply.getBrokerId());
        dto.setBrokerName(reply.getBrokerName());
        return dto;
    }

    public void setBrokerFixedProductPermission(Long brokerId, Integer status) {
        AdminSetBrokerFixedProductPermissionRequest request = AdminSetBrokerFixedProductPermissionRequest.newBuilder().setBrokerId(brokerId).setStatus(status).build();
        getStakingStub(brokerId).setBrokerFixedProductPermission(request);
    }

    public void setBrokerFixedLockProductPermission(Long brokerId, Integer status) {
        AdminSetBrokerFixedLockProductPermissionRequest request = AdminSetBrokerFixedLockProductPermissionRequest.newBuilder().setBrokerId(brokerId).setStatus(status).build();
        getStakingStub(brokerId).setBrokerFixedLockProductPermission(request);
    }

    public void  setBrokerCurrentProductPermission(Long brokerId, Integer status) {

        AdminSetBrokerCurrentProductPermissionRequest request = AdminSetBrokerCurrentProductPermissionRequest.newBuilder().setBrokerId(brokerId).setStatus(status).build();
        getStakingStub(brokerId).setBrokerCurrentProductPermission(request);
    }

}
