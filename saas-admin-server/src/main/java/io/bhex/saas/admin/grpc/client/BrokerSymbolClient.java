package io.bhex.saas.admin.grpc.client;

import io.bhex.base.bhadmin.SymbolApplyObj;
import io.bhex.base.exadmin.SymbolRecordList;
import io.bhex.base.token.*;
import io.bhex.broker.grpc.admin.GetLetfInfoRequest;
import io.bhex.broker.grpc.admin.LetfInfos;
import io.bhex.broker.grpc.admin.QuerySymbolReply;
import io.bhex.broker.grpc.admin.SymbolAgencyReply;
import io.bhex.saas.admin.controller.param.AuditPO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public interface BrokerSymbolClient {

    PublishExchangeSymbolReply publishExchangeSymbol(long exchangeId, String instanceId, String service, int port,
                                                     String symbolId, int publishValue);

    AllowExchangeSymbolTradeReply exchangeAllowTrade(long exchangeId, String symbolId, YesNoEnum yesNoEnum);

    SymbolList getAllSymbols();

    SymbolDetail getBhSymbol(String symbolId);

    io.bhex.broker.grpc.admin.SymbolDetail queryBrokerSymbolById(long brokerId, String symbolId);

    SymbolAgencyReply agencySymbol(Long exchangeId, List<String> symbolIds, Long brokerId);

    QuerySymbolReply queryBrokerSymbols(Long brokerId, Integer current, Integer pageSize, Integer category, String symbolName, List<String> symbolIds);

    Boolean closeBrokerSymbol(Long brokerId, String symbolId);

    QueryExchangeSymbolsReply queryExchangeSymbols(Long exchangeId, Integer current, Integer pageSize,  boolean agent, Integer category, String symbol);

    List<ExchangeSymbolDetail> queryExchangeSymbols(Long exchangeId, List<String> symbolIds);


    SymbolRecordList symbolApplicationList(long exchangeId, Integer current, Integer pageSize, Integer state, String symbol);

    PublishSymbolReply publishBhSymbol(PublishSymbolRequest request);

    void deleteQuoteData(Long exchangeId, String symbol);

    void copyQuoteData(Long fromExchangeId, Long toExchangeId, String symbolId);

    int changeSymbolApplyBroker(String symbolId, Long exchangeId, Long toExchangeId, Long brokerId, Long toBrokerId);

    boolean saasAllowTrade(Long exchangeId, String symbolId, boolean enabled);

    LetfInfos getLetfInfos(GetLetfInfoRequest request);
}
