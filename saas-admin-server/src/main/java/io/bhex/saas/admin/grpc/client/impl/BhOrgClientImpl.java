package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.account.ChangeOrgNameReply;
import io.bhex.base.account.ChangeOrgNameRequest;
import io.bhex.base.account.ConfigBrokerExchangeContractReply;
import io.bhex.base.account.ConfigBrokerExchangeContractRequest;
import io.bhex.base.account.ConfigMatchTransferReply;
import io.bhex.base.account.ConfigMatchTransferRequest;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.account.GetBrokerExchangeContractReply;
import io.bhex.base.account.GetBrokerExchangeContractRequest;
import io.bhex.base.account.GetEarnestAddressReply;
import io.bhex.base.account.GetEarnestAddressRequest;
import io.bhex.base.account.GetExchangesByBrokerReply;
import io.bhex.base.account.GetExchangesByBrokerRequest;
import io.bhex.base.account.GetOrgByIdReply;
import io.bhex.base.account.GetOrgByIdRequest;
import io.bhex.base.account.GetOrgByNameReply;
import io.bhex.base.account.GetOrgByNameRequest;
import io.bhex.base.account.OrgRegisterReply;
import io.bhex.base.account.OrgRegisterRequest;
import io.bhex.base.account.OrgRole;
import io.bhex.base.account.OrgServiceGrpc;
import io.bhex.base.account.OrgSwitchReply;
import io.bhex.base.account.OrgSwitchRequest;
import io.bhex.base.env.BhexEnv;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @Description:
 * @Date: 2018/9/25 下午2:18
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BhOrgClientImpl implements BhOrgClient {

    @Resource
    GrpcClientConfig grpcConfig;

    private  OrgServiceGrpc.OrgServiceBlockingStub getOrgStub() {
        return grpcConfig.orgServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    @Override
    public OrgRegisterReply orgRegisterExchange(String exchangeName, String fullName, Long saasId, Long orgId) {

        OrgRegisterRequest.Builder builder = OrgRegisterRequest.newBuilder()
                .setName(exchangeName)
                .setFullName(fullName)
                .setGroupName("")
                .setSaasId(saasId)
                .setOrgId(orgId)
                .setAreaId(800)
                .setRole(OrgRole.EXCHANGE_ROLE);
        OrgRegisterReply reply = getOrgStub().orgRegister(builder.build());

        return reply;
    }

    @Override
    public OrgRegisterReply orgRegisterBroker(String brokerName, String fullName, Long saasId, Long orgId) {
        BhexEnv env = new BhexEnv();
        long shardId = 1003l;
        if (env.isUS()) {
            shardId = 1001l;
        }
        OrgRegisterRequest.Builder builder = OrgRegisterRequest.newBuilder()
                .setName(brokerName)
                .setFullName(fullName)
                .setGroupName("")
                .setSaasId(saasId)
                .setOrgId(orgId)
                .setAreaId(800)
                .setShardId(shardId)
                .setRole(OrgRole.BROKER_ROLE);
        OrgRegisterReply reply = getOrgStub().orgRegister(builder.build());

        return reply;
    }

    @Override
    public OrgSwitchReply orgSwitch(long orgId, boolean enabled) {
        OrgSwitchRequest.Builder builder = OrgSwitchRequest.newBuilder()
                .setEnabled(enabled)
                .setOrgId(orgId);
        OrgSwitchReply reply = getOrgStub().orgSwitch(builder.build());
        return reply;
    }


    @Override
    public GetEarnestAddressReply getEarnestAddress(long orgId) {
        GetEarnestAddressRequest request = GetEarnestAddressRequest.newBuilder().setOrgId(orgId).build();
        GetEarnestAddressReply reply = getOrgStub().getEarnestAddress(request);
        return reply;
    }

    @Override
    public GetEarnestAddressReply getEarnestAddress(GetEarnestAddressRequest request) {
        return getOrgStub().getEarnestAddress(request);
    }

    @Override
    public GetOrgByNameReply getOrgByName(GetOrgByNameRequest request) {
        GetOrgByNameReply reply = getOrgStub().getOrgByName(request);
        return reply;
    }

    @Override
    public GetOrgByIdReply getOrgById(GetOrgByIdRequest request) {
        GetOrgByIdReply reply = getOrgStub().getOrgById(request);
        return reply;
    }

    @Override
    public ConfigMatchTransferReply configMatchTransfer(ConfigMatchTransferRequest request) {
        ConfigMatchTransferReply reply = getOrgStub().configMatchTransfer(request);
        return reply;
    }

    @Override
    public ConfigBrokerExchangeContractReply configBrokerExchangeContract(ConfigBrokerExchangeContractRequest request) {
        ConfigBrokerExchangeContractReply reply = getOrgStub().configBrokerExchangeContract(request);
        return reply;
    }

    @Override
    public GetBrokerExchangeContractReply getBrokerExchangeContract(GetBrokerExchangeContractRequest request) {
        return getOrgStub().getBrokerExchangeContract(request);
    }

    @Override
    public ChangeOrgNameReply changeOrgName(long orgId, String newOrgName) {
        ChangeOrgNameReply reply = getOrgStub()
                .changeOrgName(ChangeOrgNameRequest.newBuilder().setOrgId(orgId).setName(newOrgName).build());
        return reply;
    }

    public List<ExchangeReply> findAllExchangeByBrokerId(Long brokerId){

        GetExchangesByBrokerRequest req = GetExchangesByBrokerRequest.newBuilder()
                .setBrokerId(brokerId)
                .build();
        GetExchangesByBrokerReply reply = getOrgStub().getExchangesByBroker(req);
        return reply.getExchangesList();
    }

    @Override
    public ExchangeReply findExchangeByBrokerId(Long brokerId){
        List<ExchangeReply> exchanges =  findAllExchangeByBrokerId(brokerId);
        if (exchanges.size() == 0) {
            return null;
        }

        if (exchanges.size() == 1) {
            return exchanges.get(0);
        }

        Optional<ExchangeReply> optional = exchanges.stream().filter(e -> e.getIsTrust()).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return exchanges.get(0);
    }

    @Override
    public ExchangeReply findTrustExchangeByBrokerId(Long brokerId){
        List<ExchangeReply> exchanges =  findAllExchangeByBrokerId(brokerId);
        if (exchanges.size() == 0) {
            return null;
        }

        Optional<ExchangeReply> optional = exchanges.stream().filter(e -> e.getIsTrust()).findFirst();
        if (optional.isPresent()) {
            return optional.get();
        }
        return null;
    }

    @Override
    public ConfigMatchTransferReply configMatchTransfer(Long exchangeId, Long matchExchangeId,
                                                        String symbolId, Boolean enableTransfer, Integer category) {
        ConfigMatchTransferRequest request = ConfigMatchTransferRequest.newBuilder()
                .addSymbolId(symbolId)
                .setSourceExchangeId(exchangeId)
                .setMatchExchangeId(matchExchangeId)
                .setEnableTransfer(enableTransfer)
                .setCategory(category)
                .build();

        return getOrgStub().configMatchTransfer(request);
    }

}
