package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.broker.grpc.account.AccountServiceGrpc;
import io.bhex.broker.grpc.account.QuerySubAccountRequest;
import io.bhex.broker.grpc.account.SubAccount;
import io.bhex.broker.grpc.admin.*;
import io.bhex.broker.grpc.common.AccountTypeEnum;
import io.bhex.broker.grpc.common.Header;
import io.bhex.broker.grpc.user.UserServiceGrpc;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.controller.dto.BrokerUserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class BsUserClient {

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    private BrokerUserServiceGrpc.BrokerUserServiceBlockingStub getUserStub(long orgId) {
        return BrokerUserServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
    }

    private UserServiceGrpc.UserServiceBlockingStub getUserServiceStub(long orgId) {
        return UserServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
    }

    private AccountServiceGrpc.AccountServiceBlockingStub getAccountServiceStub(long orgId) {
        return AccountServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(orgId));
    }


    public BrokerUserDTO getBrokerUser(GetBrokerUserRequest request) {
        GetBrokerUserResponse response = getUserStub(request.getOrgId()).getBrokerUser(request);
        if (response.getBrokerUser().getUserId() == 0) {
            return null;
        }
        BrokerUser brokerUser = response.getBrokerUser();
        BrokerUserDTO dto = new BrokerUserDTO();
        BeanUtils.copyProperties(brokerUser, dto);
        dto.setMobile(brokerUser.getMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        dto.setEmail(brokerUser.getEmail().replaceAll("(?<=.).(?=[^@]*?.@)", "*"));
        dto.setRealEmail(brokerUser.getEmail());
        dto.setRealMobile(brokerUser.getMobile());
        dto.setIsFreezeLogin(brokerUser.getIsFreezeLogin());
        return dto;
    }

    public List<SubAccount> querySubAccount(QuerySubAccountRequest request) {
        return getAccountServiceStub(request.getHeader().getOrgId()).querySubAccount(request).getSubAccountList();
    }

    public SubAccount queryMainAccount(long orgId, long userId, AccountTypeEnum accountType) {
        QuerySubAccountRequest request = QuerySubAccountRequest.newBuilder()
                .setHeader(Header.newBuilder().setOrgId(orgId).setUserId(userId).build())
                .setAccountType(accountType)
                .build();
        List<SubAccount> list = querySubAccount(request);
        if (list.size() == 0) {
            return null;
        }
        return list.stream().filter(s -> s.getIndex() == 0).findFirst().get();
    }

    public UnfreezeUserResponse unfreezeUserLogin(Long orgId, Long userId) {
        UnfreezeUserResponse response
                = getUserStub(orgId).unfreezeUser(UnfreezeUserRequest.newBuilder().setOrgId(orgId).setUserId(userId).setType(1).build());
        return response;
    }

    public AdminUpdateEmailResponse updateUserEmail(Long orgId, Long userId, Long adminUserId, String email) {
        return getUserStub(orgId).adminUpdateEmail(AdminUpdateEmailRequest.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .setAdminUserId(adminUserId)
                .setEmail(email)
                .build());
    }
}
