package io.bhex.saas.admin.grpc.client;

import io.bhex.base.account.*;
import io.grpc.stub.StreamObserver;

/**
 * @Description:
 * @Date: 2018/9/25 下午2:18
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface BhOrgClient {

    OrgRegisterReply orgRegisterExchange(String exchangeName, String fullName, Long saasId, Long orgId);

    OrgRegisterReply orgRegisterBroker(String brokerName, String fullName, Long saasId, Long orgId);

    OrgSwitchReply orgSwitch(long orgId, boolean enabled);

    /**
     * 获取保证金地址
     *
     * @param orgId
     * @return
     */
    GetEarnestAddressReply getEarnestAddress(long orgId);

    GetEarnestAddressReply getEarnestAddress(GetEarnestAddressRequest request);

    //proxy grpc interface

    GetOrgByNameReply getOrgByName(GetOrgByNameRequest request);

    GetOrgByIdReply getOrgById(GetOrgByIdRequest request);

    ConfigMatchTransferReply configMatchTransfer(ConfigMatchTransferRequest request);

    ConfigBrokerExchangeContractReply configBrokerExchangeContract(ConfigBrokerExchangeContractRequest request);

    GetBrokerExchangeContractReply getBrokerExchangeContract(GetBrokerExchangeContractRequest request);

    ChangeOrgNameReply changeOrgName(long orgId, String newOrgName);

    ExchangeReply findTrustExchangeByBrokerId(Long brokerId);
    ExchangeReply findExchangeByBrokerId(Long brokerId);

    ConfigMatchTransferReply configMatchTransfer(Long exchangeId, Long matchExchangeId,
                                                        String symbolId, Boolean enableTransfer, Integer category);

}
