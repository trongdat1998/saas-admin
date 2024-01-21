/*
 ************************************
 * @项目名称: broker
 * @文件名称: GrcpClientConfig
 * @Date 2018/05/22
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.saas.admin.grpc.client.config;

import io.bhex.base.account.AccountServiceGrpc;
import io.bhex.base.account.BalanceServiceGrpc;
import io.bhex.base.account.BatchTransferServiceGrpc;
import io.bhex.base.account.OrderServiceGrpc;
import io.bhex.base.account.OrgServiceGrpc;
import io.bhex.base.account.WithdrawalServiceGrpc;
import io.bhex.base.admin.AdminOrgContractServiceGrpc;
import io.bhex.base.admin.AdminRoleAuthServiceGrpc;
import io.bhex.base.admin.AdminUserIpWhitelistServiceGrpc;
import io.bhex.base.admin.SecurityServiceGrpc;
import io.bhex.base.admin.common.AdminUserServiceGrpc;
import io.bhex.base.admin.common.BrokerAccountTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.BrokerTradeFeeSettingServiceGrpc;
import io.bhex.base.admin.common.BusinessLogServiceGrpc;
import io.bhex.base.admin.common.CommissionServiceGrpc;
import io.bhex.base.admin.common.CountryServiceGrpc;
import io.bhex.base.bhadmin.AdminSymbolApplyServiceGrpc;
import io.bhex.base.common.MessageServiceGrpc;
import io.bhex.base.exadmin.BrokerSmsTemplateServiceGrpc;
import io.bhex.base.exadmin.ExchangeTradeFeeSettingServiceGrpc;
import io.bhex.base.exadmin.SymbolFuturesRecordServiceGrpc;
import io.bhex.base.exadmin.SymbolRecordServiceGrpc;
import io.bhex.base.grpc.client.channel.IGrpcClientPool;
import io.bhex.base.margin.MarginConfigServiceGrpc;
import io.bhex.base.margin.cross.MarginCrossServiceGrpc;
import io.bhex.base.quote.QuoteServiceGrpc;
import io.bhex.base.token.BrokerExchangeTokenServiceGrpc;
import io.bhex.base.token.SaasTokenServiceGrpc;
import io.bhex.base.token.SymbolAdminServiceGrpc;
import io.bhex.base.token.SymbolServiceGrpc;
import io.bhex.base.token.TokenServiceGrpc;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.broker.common.entity.GrpcChannelInfo;
import io.bhex.broker.common.entity.GrpcClientProperties;
import io.bhex.broker.grpc.activity.contract.competition.AdminContractCompetitionServiceGrpc;
import io.bhex.broker.grpc.admin.AdminBrokerDBToolsServiceGrpc;
import io.bhex.broker.grpc.admin.AdminContractApplicationServiceGrpc;
import io.bhex.broker.grpc.admin.AdminCustomLabelServiceGrpc;
import io.bhex.broker.grpc.deposit.DepositServiceGrpc;
import io.bhex.broker.grpc.fee.FeeServiceGrpc;
import io.bhex.broker.grpc.function.config.BrokerFunctionConfigServiceGrpc;
import io.bhex.broker.grpc.news.NewsServiceGrpc;
import io.bhex.broker.grpc.order.ShareConfigServiceGrpc;
import io.bhex.broker.grpc.red_packet.RedPacketAdminServiceGrpc;
import io.bhex.broker.grpc.sub_business_subject.SubBusinessSubjectServiceGrpc;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component("grpcConfig")
public class GrpcClientConfig extends GrpcConfig {

    public static final String BH_SERVER_CHANNEL_NAME = "bhChannel";
    public static final String BROKER_SERVER_CHANNEL_NAME = "brokerServerChannel";
    public static final String CLEAR_CHANNEL_NAME = "clearChannel";
    public static final String QUOTE_CHANNEL_NAME = "quoteChannel";
    public static final String QUOTE_DATA_CHANNEL_NAME = "quoteDataChannel";
    public static final String MARGIN_SERVER_CHANNEL_NAME = "marginServerChannel";
    public static final String EX_ADMIN_GRPC_CHANNEL = "exAdminGrpcChannel";


    @Resource
    GrpcClientProperties grpcClientProperties;

    @Resource
    IGrpcClientPool pool;

    Long stubDeadline;

    Long shortStubDeadline;

    Long futureTimeout;

    @Override
    @PostConstruct
    public void init() {
        stubDeadline = grpcClientProperties.getStubDeadline();
        shortStubDeadline = grpcClientProperties.getShortStubDeadline();
        futureTimeout = grpcClientProperties.getFutureTimeout();
        List<GrpcChannelInfo> channelInfoList = grpcClientProperties.getChannelInfo();
        for (GrpcChannelInfo channelInfo : channelInfoList) {
            pool.setShortcut(channelInfo.getChannelName(), channelInfo.getHost(), channelInfo.getPort());
        }
    }


    @Override
    public AdminRoleAuthServiceGrpc.AdminRoleAuthServiceBlockingStub adminRoleAuthServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminRoleAuthServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public SecurityServiceGrpc.SecurityServiceBlockingStub securityServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SecurityServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public CommissionServiceGrpc.CommissionServiceBlockingStub commissionServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return CommissionServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public AdminUserServiceGrpc.AdminUserServiceBlockingStub adminUserServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminUserServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public BusinessLogServiceGrpc.BusinessLogServiceBlockingStub businessLogServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BusinessLogServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public AdminUserIpWhitelistServiceGrpc.AdminUserIpWhitelistServiceBlockingStub adminUserIpWhitelistServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminUserIpWhitelistServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public CountryServiceGrpc.CountryServiceBlockingStub countryServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return CountryServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public SaasTokenServiceGrpc.SaasTokenServiceBlockingStub saasTokenServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SaasTokenServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BalanceServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public AccountServiceGrpc.AccountServiceBlockingStub accountServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AccountServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceBlockingStub brokerTradeFeeSettingServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BrokerTradeFeeSettingServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public BrokerSmsTemplateServiceGrpc.BrokerSmsTemplateServiceBlockingStub brokerSmsTemplateServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BrokerSmsTemplateServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public io.bhex.base.clear.CommissionServiceGrpc.CommissionServiceBlockingStub clearCommissionServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return io.bhex.base.clear.CommissionServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public MessageServiceGrpc.MessageServiceBlockingStub messageServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return MessageServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    @Override
    public BrokerAccountTradeFeeSettingServiceGrpc.BrokerAccountTradeFeeSettingServiceBlockingStub brokerAccountTradeFeeSettingServiceBlockingStub(String channelName){
        Channel channel = pool.borrowChannel(channelName);
        return BrokerAccountTradeFeeSettingServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public QuoteServiceGrpc.QuoteServiceBlockingStub quoteServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return QuoteServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public OrgServiceGrpc.OrgServiceBlockingStub orgServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return OrgServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public TokenServiceGrpc.TokenServiceBlockingStub tokenServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return TokenServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public WithdrawalServiceGrpc.WithdrawalServiceBlockingStub withdrawalServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return WithdrawalServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public io.bhex.broker.grpc.order.OrderServiceGrpc.OrderServiceBlockingStub brokerOrderServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return io.bhex.broker.grpc.order.OrderServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public OrderServiceGrpc.OrderServiceBlockingStub orderServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return OrderServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminSymbolApplyServiceGrpc.AdminSymbolApplyServiceBlockingStub adminSymbolApplyServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminSymbolApplyServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public FeeServiceGrpc.FeeServiceBlockingStub feeServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return FeeServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public io.bhex.base.account.FeeServiceGrpc.FeeServiceBlockingStub saasFeeServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return io.bhex.base.account.FeeServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public DepositServiceGrpc.DepositServiceBlockingStub depositServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return DepositServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public io.bhex.base.account.DepositServiceGrpc.DepositServiceBlockingStub saasDepositServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return io.bhex.base.account.DepositServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminContractApplicationServiceGrpc.AdminContractApplicationServiceBlockingStub adminContractApplicationServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminContractApplicationServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BatchTransferServiceGrpc.BatchTransferServiceBlockingStub batchTransferServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BatchTransferServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminContractCompetitionServiceGrpc.AdminContractCompetitionServiceBlockingStub adminContractCompetitionServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminContractCompetitionServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminCustomLabelServiceGrpc.AdminCustomLabelServiceBlockingStub adminCustomLabelServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminCustomLabelServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminOrgContractServiceGrpc.AdminOrgContractServiceBlockingStub adminOrgContractServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return AdminOrgContractServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public NewsServiceGrpc.NewsServiceBlockingStub newsServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return NewsServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BrokerFunctionConfigServiceGrpc.BrokerFunctionConfigServiceBlockingStub brokerFunctionConfigServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return BrokerFunctionConfigServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public RedPacketAdminServiceGrpc.RedPacketAdminServiceBlockingStub redPacketAdminServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return RedPacketAdminServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public ShareConfigServiceGrpc.ShareConfigServiceBlockingStub shareConfigServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return ShareConfigServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SymbolAdminServiceGrpc.SymbolAdminServiceBlockingStub symbolAdminServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SymbolAdminServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SymbolServiceGrpc.SymbolServiceBlockingStub symbolServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SymbolServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SubBusinessSubjectServiceGrpc.SubBusinessSubjectServiceBlockingStub subBusinessSubjectServiceBlockingStub(String channelName) {
        Channel channel = pool.borrowChannel(channelName);
        return SubBusinessSubjectServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public AdminBrokerDBToolsServiceGrpc.AdminBrokerDBToolsServiceBlockingStub adminBrokerDBToolsServiceBlockingStub() {
        Channel channel = pool.borrowChannel(BROKER_SERVER_CHANNEL_NAME);
        return AdminBrokerDBToolsServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public BrokerExchangeTokenServiceGrpc.BrokerExchangeTokenServiceBlockingStub brokerExchangeTokenServiceBlockingStub() {
        Channel channel = pool.borrowChannel(BH_SERVER_CHANNEL_NAME);
        return BrokerExchangeTokenServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public MarginCrossServiceGrpc.MarginCrossServiceBlockingStub marginCrossServiceBlockingStub() {
        Channel channel = pool.borrowChannel(BH_SERVER_CHANNEL_NAME);
        return MarginCrossServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public ExchangeTradeFeeSettingServiceGrpc.ExchangeTradeFeeSettingServiceBlockingStub exchangeTradeFeeSettingServiceBlockingStub() {
        Channel channel = pool.borrowChannel(BH_SERVER_CHANNEL_NAME);
        return ExchangeTradeFeeSettingServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SymbolRecordServiceGrpc.SymbolRecordServiceBlockingStub symbolRecordServiceBlockingStub() {
        Channel channel = pool.borrowChannel(EX_ADMIN_GRPC_CHANNEL);
        return SymbolRecordServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public SymbolFuturesRecordServiceGrpc.SymbolFuturesRecordServiceBlockingStub symbolFuturesRecordServiceBlockingStub() {
        Channel channel = pool.borrowChannel(EX_ADMIN_GRPC_CHANNEL);
        return SymbolFuturesRecordServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

    public MarginConfigServiceGrpc.MarginConfigServiceBlockingStub marginConfigServiceBlockingStub() {
        Channel channel = pool.borrowChannel(MARGIN_SERVER_CHANNEL_NAME);
        return MarginConfigServiceGrpc.newBlockingStub(channel).withDeadlineAfter(stubDeadline, TimeUnit.MILLISECONDS);
    }

}
