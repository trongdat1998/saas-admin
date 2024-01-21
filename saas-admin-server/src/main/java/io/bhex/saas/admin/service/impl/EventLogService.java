package io.bhex.saas.admin.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.bhex.base.bhadmin.QueryUpdatingSymbolsResult;
import io.bhex.saas.admin.dao.EventLogMapper;
import io.bhex.saas.admin.enums.EventTypeEnum;
import io.bhex.saas.admin.model.EventLog;
import io.bhex.saas.admin.service.BrokerSymbolService;
import io.bhex.saas.admin.service.ExchangeSwapService;
import io.bhex.saas.admin.util.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventLogService {
    @Resource
    private EventLogMapper eventLogMapper;
    @Resource
    private BrokerSymbolService brokerSymbolService;
    @Resource
    private ExchangeSwapService exchangeSwapService;
    @Resource
    private LetfService letfService;

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    public EventLog addEvent(long brokerId, long requestId, String requestInfo, EventTypeEnum eventType) {
        EventLog eventLog = EventLog.builder()
                .cmd(eventType.name())
                .brokerId(brokerId)
                .requestId(requestId)
                .requestInfo(requestInfo)
                .created(new Timestamp(System.currentTimeMillis()))
                .updated(new Timestamp(System.currentTimeMillis()))
                .build();
        eventLogMapper.insertSelective(eventLog);
        return eventLog;
    }

    public boolean hasUndoneTask(long brokerId, long requestId) {
        return eventLogMapper.getStatus0Event(brokerId, requestId) != null;
    }

    public boolean hasUndoneTask(long brokerId, String requestInfo) {
        return eventLogMapper.getStatus0Event2(brokerId, requestInfo) != null;
    }

    public List<EventLog> getUnDoneTasks() {
        return eventLogMapper.getUnDoneTask();
    }

    public EventLog getUnSuccessEvent(long brokerId, long requestId) {
        EventLog eventLog = eventLogMapper.getEventLog(brokerId, requestId);
        return eventLog != null && eventLog.getStatus() != 1 ? eventLog : null;
    }



    public void eventEnd(long eventId) {
        eventLogMapper.eventEnd(eventId, new Timestamp(System.currentTimeMillis()));
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void eventExecutor() {
        List<EventLog> list = getUnDoneTasks();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Collections.shuffle(list);
        for(EventLog eventLog : list) {
            try {
                doEvent(eventLog);
            } catch (Exception e) {
                log.warn("{}", eventLog, e);
            }
        }
    }

    public void doEvent(EventLog eventLog) {
        String lockKey = "event." + eventLog.getId();
        boolean lock = RedisLockUtils.tryLock(redisTemplate, lockKey, 9_000);
        if (!lock) {
            //log.info("{} exec in other pod", eventLog);
            return;
        }

        if (System.currentTimeMillis() - eventLog.getCreated().getTime() > 600_000L) {
            eventLog.setStatus(2);
            eventLog.setUpdated(new Timestamp(System.currentTimeMillis()));
            eventLog.setRemark("not finished in 10min");
            eventLogMapper.updateByPrimaryKeySelective(eventLog);
            log.error("{} not finished in 10min.", eventLog);
            return;
        }
        if (eventLog.getCmd().equals(EventTypeEnum.AUDIT_SYMBOL_SUCCESS.name())) {
            brokerSymbolService.auditSymbolFull(eventLog.getRequestId(), eventLog.getId());
        } else if (eventLog.getCmd().equals(EventTypeEnum.PUBLISH_BROKER_SYMBOL.name())) {
            brokerSymbolService.publishBrokerSymbol(eventLog.getBrokerId(), eventLog.getRequestInfo(), eventLog.getId());
        } else if (eventLog.getCmd().equals(EventTypeEnum.AUDIT_CONTRACT_SUCCESS.name())) {
            exchangeSwapService.auditSymbolFull(eventLog.getRequestId(), eventLog.getId());
        } else if (eventLog.getCmd().equals(EventTypeEnum.CLOSE_BROKER_SYMBOL.name())) {
            brokerSymbolService.closeSymbol(eventLog.getBrokerId(), eventLog.getRequestInfo(), eventLog.getId());
        } else if (eventLog.getCmd().equals(EventTypeEnum.CANCEL_LETF_ORDERS.name())) {
            letfService.cancelAllLetfOrders(eventLog.getId(), eventLog.getRequestInfo());
        }
    }



    public QueryUpdatingSymbolsResult getUpdatingSymbols(long brokerId, List<String> symbols) {
        if (brokerId == 0 || CollectionUtils.isEmpty(symbols)) {
            return QueryUpdatingSymbolsResult.getDefaultInstance();
        }

        QueryUpdatingSymbolsResult.Builder builder = QueryUpdatingSymbolsResult.newBuilder();
        for (String symbol : symbols) {
            String key = brokerId + "_" + symbol;
            int value = SYMBOL_UPDATING_MAP.getOrDefault(key, 0);
            builder.putResult(symbol, value);
        }
        return builder.build();
    }

    //0-没有更新 1-上架中 2-上架失败 3-下架中 4-下架失败
    private Map<String, Integer> SYMBOL_UPDATING_MAP = Maps.newHashMap();
    private Timestamp lastModified = new Timestamp(0);
    @Scheduled(cron = "0/1 * * * * ?")
    public void loadUpdatingSymbols() {
        Example example =  Example.builder(EventLog.class)
                .orderByAsc("id")
                .build();
        Example.Criteria criteria =   example.createCriteria()
                .andIn("cmd", Lists.newArrayList(EventTypeEnum.CLOSE_BROKER_SYMBOL.name(), EventTypeEnum.PUBLISH_BROKER_SYMBOL.name()))
                .andGreaterThan("updated", lastModified);
        List<EventLog> eventLogs = eventLogMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(eventLogs)) {
            return;
        }
        eventLogs.forEach(e -> {
            String key = e.getBrokerId() + "_" + e.getRequestInfo();
            if (e.getStatus() == 1) {
                SYMBOL_UPDATING_MAP.put(key, 0);
            } else {
                if (e.getCmd().equals(EventTypeEnum.PUBLISH_BROKER_SYMBOL.name())) {
                    SYMBOL_UPDATING_MAP.put(key, e.getStatus() == 0 ? 1 : 2);
                } else {
                    SYMBOL_UPDATING_MAP.put(key, e.getStatus() == 0 ? 3 : 4);
                }
            }
            lastModified = e.getUpdated();
        });
    }
}
