package io.bhex.saas.admin.service.impl;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import io.bhex.base.account.CancelSymbolOrdersReply;
import io.bhex.base.account.MergeBalanceRequest;
import io.bhex.base.account.MergeBalanceResponse;
import io.bhex.base.admin.common.BusinessLog;
import io.bhex.base.proto.ErrorCode;
import io.bhex.base.token.ExchangeSymbolDetail;
import io.bhex.base.token.SymbolDetail;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.grpc.client.BusinessLogClient;
import io.bhex.broker.common.util.JsonUtil;
import io.bhex.broker.grpc.admin.GetLetfInfoRequest;
import io.bhex.broker.grpc.admin.LetfInfos;
import io.bhex.saas.admin.dao.EventLogMapper;
import io.bhex.saas.admin.enums.EventTypeEnum;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.impl.GrpcBalanceClient;
import io.bhex.saas.admin.grpc.client.impl.OrderClient;
import io.bhex.saas.admin.model.EventLog;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LetfService {

    @Resource
    private OrderClient orderClient;
    @Resource
    private EventLogService eventLogService;
    @Resource
    private EventLogMapper eventLogMapper;
    @Resource
    private BrokerSymbolClient brokerSymbolClient;
    @Resource
    private GrpcBalanceClient grpcBalanceClient;
    @Resource
    private BusinessLogClient businessLogClient;

    public List<Map<String, Object>> getLetfInfos(List<String> symbols) {
        GetLetfInfoRequest request = GetLetfInfoRequest.newBuilder().addAllSymbolId(symbols)
                .setOrgId(6002L)
                .build();
        LetfInfos letfInfos = brokerSymbolClient.getLetfInfos(request);
        if (CollectionUtils.isEmpty(letfInfos.getLetfInfoList())) {
            return Lists.newArrayList();
        }
        List<ExchangeSymbolDetail> exchangeSymbolDetails = brokerSymbolClient.queryExchangeSymbols(301L, symbols);
        if (CollectionUtils.isEmpty(exchangeSymbolDetails)) {
            return Lists.newArrayList();
        }
        Map<String, ExchangeSymbolDetail> exchangeSymbolMap = exchangeSymbolDetails.stream()
                .collect(Collectors.toMap(l -> l.getSymbolId(), l -> l));


        List<Map<String, Object>> result = Lists.newArrayList();
        Map<String, LetfInfos.LetfInfo> groupMap = letfInfos.getLetfInfoList().stream()
                .collect(Collectors.toMap(l -> l.getSymbolId(), l -> l));
        for (String symbolId : symbols) {
            Map<String, Object> item = Maps.newHashMap();
            item.put("symbolId", symbolId);
            LetfInfos.LetfInfo letfInfo = groupMap.get(symbolId);
            if (letfInfo == null) {
                continue;
            }
            ExchangeSymbolDetail symbolDetail = exchangeSymbolMap.get(symbolId);
            if (symbolDetail != null) {
                item.put("saasAllowTradeStatus", symbolDetail.getSaasAllowTradeStatus());
            } else {
                item.put("saasAllowTradeStatus", false);
            }

            item.put("marketAccounts", letfInfo.getMarketAccountsList());
            if (!Strings.isNullOrEmpty(letfInfo.getHoldQuantity())) {
                item.put("holdQuantityLimit", letfInfo.getHoldQuantity());
            } else {
                item.put("holdQuantityLimit", "0");
            }

            if (!Strings.isNullOrEmpty(letfInfo.getWhiteListUserId())) {
                item.put("whiteListUsers", letfInfo.getWhiteListUserId().split(","));
            }

            //item.put("tokenId", letfInfo.getTokenId());
            result.add(item);
        }
        return result;
    }

    public List<OperateRecord> getOperationList(String symbolId) {
        io.bhex.base.admin.common.QueryLogsRequest.Builder builder = io.bhex.base.admin.common.QueryLogsRequest.newBuilder()
                .addAllOpTypes(Lists.newArrayList("switchLetfSymbolTrade", "cancelLetfSymbolOrder", "mergeLetfSymbolBalance"))
                .setOrgId(0)
                .setWithRequestInfo(true)
                .addEntityIds(symbolId);
        List<BusinessLog> logs = businessLogClient.queryLogs(builder.build());
        if(CollectionUtils.isEmpty(logs)) {
            return Lists.newArrayList();
        }
        Example example =  Example.builder(EventLog.class)
                .orderByAsc("id")
                .build();
        Example.Criteria criteria =   example.createCriteria()
                .andIn("cmd", Lists.newArrayList(EventTypeEnum.CANCEL_LETF_ORDERS.name()))
                .andEqualTo("requestInfo", symbolId);
        List<EventLog> eventLogs = eventLogMapper.selectByExample(example);
        List<OperateRecord> records = Lists.newArrayList();
        for (BusinessLog log : logs) {
            OperateRecord record = new OperateRecord();
            record.setOpType(log.getOpType().replace("LetfSymbol", ""));
            record.setCreated(log.getCreated());
            Map<String, Object> req = JsonUtil.defaultGson().fromJson(log.getRequestInfo(), Map.class);
            if (log.getOpType().equals("switchLetfSymbolTrade")) {
                if (req.containsKey("enabled") && req.get("enabled").toString().equals("true")) {
                    record.setInfo("OpenTrade");
                } else {
                    record.setInfo("CloseTrade");
                }
            } else if (log.getOpType().equals("mergeLetfSymbolBalance")) {
                record.setInfo("MergeTimes:" + req.get("mergeTimes"));
            }
            records.add(record);
        }
        for (EventLog log : Lists.newArrayList(eventLogs)) {
            OperateRecord record = new OperateRecord();
            record.setOpType(log.getCmd().equals(EventTypeEnum.CANCEL_LETF_ORDERS.name()) ? "cancelOrder" : log.getCmd());
            record.setCreated(log.getUpdated().getTime());
            if (log.getStatus() == 0) {
                record.setInfo("doing");
            } else if (log.getStatus() == 1) {
                record.setInfo("done");
            } else if (log.getStatus() == 2) {
                record.setInfo("failed");
            }
            records.add(record);
        }
        return records;
    }

    @Data
    private static class OperateRecord {
        private String opType;
        private Long created;
        private String info;
    }
    public void stopTrade(String symbolId) {
        brokerSymbolClient.saasAllowTrade(301L, symbolId, false);
    }

    public void allowTrade(String symbolId) {
        brokerSymbolClient.saasAllowTrade(301L, symbolId, true);
    }

    public void cancelOrders(String symbolId) {
        if (eventLogService.hasUndoneTask(0, symbolId)) {
            throw new BizException(symbolId + " task is doing.");
        }
        eventLogService.addEvent(0, 0, symbolId, EventTypeEnum.CANCEL_LETF_ORDERS);
    }

    public void mergeBalance(String symbolId, BigDecimal mergeRate) {
        String date = new DateTime().toString("yyyyMMddHH");
        long clientReqId = Math.abs((symbolId + date).hashCode());
        SymbolDetail symbolDetail = brokerSymbolClient.getBhSymbol(symbolId);
        if (!symbolDetail.getSaasAllowTradeStatus()) {
            throw new BizException(symbolId + " in trading status!");
        }

        String tokenId = symbolDetail.getBaseTokenId();
        MergeBalanceRequest request = MergeBalanceRequest.newBuilder()
                .setClientReqId(clientReqId)
                .setTokenId(tokenId)
                .setMergeRate(mergeRate.stripTrailingZeros().toPlainString())
                .build();
        MergeBalanceResponse response = grpcBalanceClient.mergeBalance(request);
        log.info("{} {} {}", date, TextFormat.shortDebugString(request), TextFormat.shortDebugString(response));
        if (response.getResponse().getCode() != ErrorCode.SUCCESS) {
            throw new BizException("merge balance error" + TextFormat.shortDebugString(response));
        }
    }

    public void cancelAllLetfOrders(long eventId, String symbolId) {
        CancelSymbolOrdersReply reply = orderClient.cancelBrokerOrderNew(0, symbolId);
        if (reply.getOpenOrdersCount() > 0) {
            log.info("cancel letf order not end. {} result:{}", symbolId, TextFormat.shortDebugString(reply));
        } else {
            log.info("cancel broker:{} {} orders end", 0, symbolId);
            eventLogService.eventEnd(eventId);
        }
    }


}
