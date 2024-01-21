package io.bhex.saas.admin.service;

import io.bhex.base.bhadmin.*;
import io.bhex.base.token.*;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.saas.admin.controller.dto.BrokerSymbolDTO;
import io.bhex.saas.admin.controller.dto.SymbolRecordDTO;
import io.bhex.saas.admin.controller.param.AuditPO;
import io.bhex.saas.admin.controller.param.BatchAllowSymbolPO;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Date: 2018/11/5 上午10:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public interface BrokerSymbolService {

    void copySymbolQuote(Long fromExchangeId, Long toExchangeId, String symbolId);

    void deleteQuoteData(Long exchangeId, String symbolId);

    SymbolDetail getSymbolBySymbolId(String symbolId);

    void syncExchangeTokens(Long exchangeId);

    SymbolApplyRecordList listApplyRecords(GetSymbolPager request);

    PaginationVO<SymbolRecordDTO> applicationList(long brokerId, Integer current, Integer pageSize, Integer state, String symbol);

    PaginationVO<BrokerSymbolDTO> query(Long brokerId, Integer current, Integer pageSize, Integer category, boolean agent, String symbol);

   // PaginationVO<ExchangeSymbolDTO> queryMySymbols(Long exchangeId, Integer current, Integer pageSize, Integer category);
    /**
     * 修改币对在某个交易所中的显示状态
     * @param exchangeId
     * @param symbolId
     * @param showInExchange
     * @return
     */
    boolean updateShowStatusInExchange(Long exchangeId, String symbolId, boolean showInExchange);

    AllowExchangeSymbolTradeReply exchangeAllowTrade(long exchangeId, String symbolId, YesNoEnum yesNoEnum);
    /**
     *
     * @param exchangeId
     * @param symbolId
     * @return suc,service
     */
    Combo2<Boolean, String> publishExchangeSymbol(Long exchangeId, String symbolId, String service);
    /**
     * 查询交易的币对信息
     * @param exchangeId
     * @param symbolId
     * @param current
     * @param pageSize
     * @param myAgentSymbol
     * @return
     */
    QueryExchangeSymbolsReply queryExchangeSymbols(Long exchangeId, String symbolId, Integer current,
                                                   Integer pageSize, boolean myAgentSymbol, Integer category);
    /**
     * 查询交易的币对信息
     * @param exchangeId
     * @param symbolId
     * @param current
     * @param pageSize
     * @return
     */
    QueryAgentFuturesReply queryAgentFutures(Long exchangeId, String symbolId, Integer current,
                                             Integer pageSize);

    /**
     * 查询券商合作交易所的币对信息
     * @param exchangeId
     * @param current
     * @param pageSize
     * @return
     */
    QueryBrokerExchangeSymbolsReply queryBrokerExchangeSymbols(Long exchangeId, Integer current,
                                                               Integer pageSize, Integer category);

    int auditSymbolRecord(long brokerId, AuditPO auditPO);

    void changeSymbolBroker(Long brokerId, String symbolId, Long toBrokerId);

    boolean saasAllowTrade(Long exchangeId, String symbolId, boolean enabled);

    //ApplySymbolResult auditApplySymbol(AuditSymbolApplyRequest request);

    //审核通过全流程
    void auditSymbolFull(long applyId, long eventId);

    OpenSymbolResult publishBrokerSymbol(long brokerId, String symbolId, long eventId);

    void batchPublishBrokerSymbol(BatchAllowSymbolPO po);
    void batchCloseBrokerSymbol(BatchAllowSymbolPO po);
    CloseSymbolResult closeSymbol(long brokerId, String symbolId, long eventId);
}
