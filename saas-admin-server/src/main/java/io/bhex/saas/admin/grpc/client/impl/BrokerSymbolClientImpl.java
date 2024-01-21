package io.bhex.saas.admin.grpc.client.impl;

import com.google.common.base.Strings;
import io.bhex.base.exadmin.*;
import io.bhex.base.exception.ErrorStatusRuntimeException;
import io.bhex.base.proto.ErrorStatus;
import io.bhex.base.quote.CommonResponse;
import io.bhex.base.quote.GetQuoteRequest;
import io.bhex.base.quote.QuoteServiceGrpc;
import io.bhex.base.token.*;
import io.bhex.base.token.SymbolDetail;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.broker.grpc.admin.*;
import io.bhex.ex.quote.CopyQuoteResponse;
import io.bhex.ex.quote.service.IQuoteAdminService;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.grpc.Deadline;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@Service
public class BrokerSymbolClientImpl implements BrokerSymbolClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    @Autowired
    private IQuoteAdminService quoteAdminService;

    private QuoteServiceGrpc.QuoteServiceBlockingStub getQuoteStub() {
        return grpcConfig.quoteServiceBlockingStub(GrpcClientConfig.QUOTE_CHANNEL_NAME);
    }

    private SymbolServiceGrpc.SymbolServiceBlockingStub getStub() {
        return grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private SymbolAdminServiceGrpc.SymbolAdminServiceBlockingStub getBhAdminStub() {
        return grpcConfig.symbolAdminServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private SymbolRecordServiceGrpc.SymbolRecordServiceBlockingStub getCommonStub() {
        return grpcConfig.symbolRecordServiceBlockingStub();
    }

    private AdminSymbolServiceGrpc.AdminSymbolServiceBlockingStub getBrokerSymbolStub(long brokerId) {
        return AdminSymbolServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(brokerId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }

    @Override
    public PublishExchangeSymbolReply publishExchangeSymbol(long exchangeId, String instanceId, String service, int port,
                                                            String symbolId, int publishValue) {

        PublishExchangeSymbolRequest request = PublishExchangeSymbolRequest.newBuilder()
            .setPublish(YesNoEnum.forNumber(publishValue))
            .setExchangeId(exchangeId)
            .setSymbolId(symbolId)
            .setHost(instanceId).setService(service)
            .setPort(port)
            .build();
        PublishExchangeSymbolReply reply = getStub().publishExchangeSymbol(request);
        log.info("request:{} response:{}", request, reply);
        return reply;
    }

    @Override
    public AllowExchangeSymbolTradeReply exchangeAllowTrade(long exchangeId, String symbolId, YesNoEnum yesNoEnum) {
        AllowExchangeSymbolTradeRequest request = AllowExchangeSymbolTradeRequest.newBuilder()
                .setAllowTrade(yesNoEnum)
                .setExchangeId(exchangeId)
                .setSymbolId(symbolId)
                .build();
        AllowExchangeSymbolTradeReply reply = getStub().allowExchangeSymbolTrade(request);
        log.info("request:{} response:{}", request, reply);
        return reply;
    }


    @Override
    public SymbolList getAllSymbols() {

        GetSymbolListRequest.Builder builder = GetSymbolListRequest.newBuilder();

        return getStub().getSymbolList(builder.build());
    }

    @Override
    public SymbolDetail getBhSymbol(String symbolId) {

        GetSymbolRequest.Builder builder = GetSymbolRequest.newBuilder()
            .setSymbolId(symbolId).setForceFromDb(true);
        try {
            return getStub().getSymbol(builder.build());
        } catch (StatusRuntimeException e) {
            ErrorStatus errorStatus = e.getTrailers().get(ErrorStatusRuntimeException.ERROR_STATUS_KEY);
            if (errorStatus != null) {
                io.bhex.base.proto.ErrorCode error = errorStatus.getCode();
                log.warn("getSymbol error: symbolId => {}", symbolId, e);
                if (error == io.bhex.base.proto.ErrorCode.ERR_Invalid_Argument) {
                    return null;
                }
            }
        }
        throw new BizException(ErrorCode.SYMBOL_MISSING);
    }

    @Override
    public io.bhex.broker.grpc.admin.SymbolDetail queryBrokerSymbolById(long brokerId, String symbolId) {
        return getBrokerSymbolStub(brokerId).queryOneSymbol(QueryOneSymbolRequest.newBuilder()
                .setBrokerId(brokerId)
                .setSymbolId(symbolId)
                .build());
    }

    @Override
    public SymbolAgencyReply agencySymbol(Long exchangeId, List<String> symbolIds, Long brokerId) {
        SymbolAgencyRequest request = SymbolAgencyRequest.newBuilder()
                .setExchangeId(exchangeId)
                .addAllSymbolId(symbolIds)
                .setBrokerId(brokerId)
                .build();

        return getBrokerSymbolStub(brokerId).symbolAgency(request);
    }

    @Override
    public QuerySymbolReply queryBrokerSymbols(Long brokerId, Integer current, Integer pageSize, Integer category, String symbolName, List<String> symbolIds) {
        QuerySymbolRequest.Builder builder = QuerySymbolRequest.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize)
                .setBrokerId(brokerId);
        if (!StringUtils.isEmpty(symbolName)) {
            builder.setSymbolName(Strings.nullToEmpty(symbolName).replace("/", ""));
        }
        if (category != null && category > 0) {
            builder.setCategory(category);
        }
        if (!CollectionUtils.isEmpty(symbolIds)) {
            builder.addAllSymbolId(symbolIds);
        }
        return getBrokerSymbolStub(brokerId).querySymbol(builder.build());
    }

    @Override
    public Boolean closeBrokerSymbol(Long brokerId, String symbolId) {
        SymbolPublishRequest request = SymbolPublishRequest.newBuilder()
                .setSymbolId(symbolId)
                .setPublish(false)
                .setBrokerId(brokerId)
                .build();

        return getBrokerSymbolStub(brokerId).symbolPublish(request).getResult();
    }

    @Override
    public List<ExchangeSymbolDetail> queryExchangeSymbols(Long exchangeId, List<String> symbolIds) {
        QueryExchangeSymbolsByIdsRequest request = QueryExchangeSymbolsByIdsRequest.newBuilder()
            .setExchangeId(exchangeId)
            .addAllSymbolIds(symbolIds)
            .build();
        QueryExchangeSymbolsByIdsReply bhreply = getStub().queryExchangeSymbolsByIds(request);
        List<ExchangeSymbolDetail> list = bhreply.getExchangeSymbolDetailsList();
        return list;
    }

    @Override
    public SymbolRecordList symbolApplicationList(long exchangeId, Integer current, Integer pageSize, Integer state, String symbol) {
        GetSymbolPager pager = GetSymbolPager.newBuilder()
            .setStart(current)
            .setSize(pageSize)
            .setState(state)
            .setExchangeId(exchangeId <= 0 ? -1 : exchangeId)
            .setSymbol(Strings.nullToEmpty(symbol))
            .build();
        return getCommonStub().listSymbolRecord(pager);
    }

    @Override
    public PublishSymbolReply publishBhSymbol(PublishSymbolRequest request) {
        return getStub().publishSymbol(request);
    }

    @Override
    public void deleteQuoteData(Long exchangeId, String symbol) {
        CommonResponse response = getQuoteStub().deleteQuoteData(GetQuoteRequest.newBuilder()
            .setExchangeId(exchangeId)
            .setSymbol(symbol)
            .build());
        if (response.getCode() == -10001) {
            throw new BizException(ErrorCode.SYMBOL_STATE_ERROR);
        }
    }

    @Override
    public void copyQuoteData(Long fromExchangeId, Long toExchangeId, String symbolId) {
        CopyQuoteResponse response = quoteAdminService.copyQuote(fromExchangeId, toExchangeId, symbolId);
        log.info("Copy quote data from [{}] to [{}] symbol [{}] res [{}]",
            fromExchangeId, toExchangeId, symbolId, response);
    }

    @Override
    public int changeSymbolApplyBroker(String symbolId, Long exchangeId, Long toExchangeId, Long brokerId, Long toBrokerId) {
        ChangeSymbolApplyBrokerReply result = getBhAdminStub()
            .changeSymbolApplyBroker(ChangeSymbolApplyBrokerRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setToExchangeId(toExchangeId)
                .setSymbolId(symbolId)
                    .setBrokerId(brokerId)
                    .setToBrokerId(toBrokerId)
                .build());
        return result.getResult();
    }

    @Override
    public QueryExchangeSymbolsReply queryExchangeSymbols(Long exchangeId, Integer current, Integer pageSize, boolean agent, Integer category, String symbol) {

        QueryExchangeSymbolsRequest.Builder builder = QueryExchangeSymbolsRequest.newBuilder()
            .setCurrent(current)
            .setPageSize(pageSize)
            .setCategory(category)
            .setSymbolId(Strings.nullToEmpty(symbol))
            .setMyAgentSymbol(agent);
        if (exchangeId != null) {
            builder.setExchangeId(exchangeId);
        }

        return getStub().queryExchangeSymbols(builder.build());
    }

    @Override
    public boolean saasAllowTrade(Long exchangeId, String symbolId, boolean enabled) {
        SaasAllowTradeExSymbolRequest request = SaasAllowTradeExSymbolRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setSymbolId(symbolId)
                .setAllowTrade(enabled ? YesNoEnum.YES : YesNoEnum.NO)
                .build();
        SymbolAdminServiceGrpc.SymbolAdminServiceBlockingStub stub = getBhAdminStub();
        SaasAllowTradeExSymbolReply reply = stub.saasAllowTradeExSymbol(request);
        return reply.getResult() == 0;
    }

    @Override
    public LetfInfos getLetfInfos(GetLetfInfoRequest request) {
        return getBrokerSymbolStub(0).getLetfInfos(request);
    }
}
