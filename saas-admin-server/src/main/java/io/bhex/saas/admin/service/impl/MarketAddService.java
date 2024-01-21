package io.bhex.saas.admin.service.impl;

import io.bhex.broker.common.util.JsonUtil;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.http.param.MarketAddPO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.util.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MarketAddService {

    private static final String EX_SYMBOL_HASHKEY = "saas.symbol.marketadd";

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ExchangeInfoService exchangeInfoService;


    public void marketAdd(MarketAddPO marketAddPO) {
        if (System.currentTimeMillis() - marketAddPO.getFirstReqTime() > 600_000) {
            log.error("marketAdd Error retry 10 times, pls contact admin req:{}", marketAddPO);
            redisTemplate.opsForHash().delete(EX_SYMBOL_HASHKEY, buildExSymbolKey(marketAddPO.getExchangeId(), marketAddPO.getSymbolId()));
            return;
        }
        ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(marketAddPO.getExchangeId());
        ExchangeResultRes<Map<String, Long>> result = FeignConfig.getExchangeGatewayClient(instanceDetail.getGatewayUrl()).marketAdd(marketAddPO);
        log.info("market add request:{} result: {}", marketAddPO, result);
        if (result.getStatus() != 200 && result.getStatus() != 1304) { //成功插入或者已经存在都认为成功
            log.warn("marketAdd Error req:{} res:{}", marketAddPO, result);
            redisTemplate.opsForHash().put(EX_SYMBOL_HASHKEY, buildExSymbolKey(marketAddPO.getExchangeId(), marketAddPO.getSymbolId()),
                    JsonUtil.defaultGson().toJson(marketAddPO));
        } else {
            redisTemplate.opsForHash().delete(EX_SYMBOL_HASHKEY, buildExSymbolKey(marketAddPO.getExchangeId(), marketAddPO.getSymbolId()));
        }
    }

    private String buildExSymbolKey(long exchangeId, String symbolId) {
        return exchangeId + "$" + symbolId;
    }

    //新审核的币对不能直接上撮合，要等1分钟后再上
    @Scheduled(cron = "16 * * * * ?")
    public void newSymbolToPublish() {
        Map<Object, Object> valuesMap = redisTemplate.opsForHash().entries(EX_SYMBOL_HASHKEY);
        if (valuesMap == null || valuesMap.size() == 0) {
            return;
        }
        List<Object> sets = valuesMap.keySet().stream().collect(Collectors.toList());
        Collections.shuffle(sets);

        for (Object symbolIdObj : sets) {
            String lockKey = "newSymbolToPublish" + symbolIdObj.toString();
            boolean lock = RedisLockUtils.tryLock(redisTemplate, lockKey, 30_000);
            if (!lock) {
                log.info("{} exec in other pod", symbolIdObj.toString());
                continue;
            }
            Object marketPoObject = redisTemplate.opsForHash().get(EX_SYMBOL_HASHKEY, symbolIdObj.toString());
            if (marketPoObject == null) {
                log.info("{} finished", symbolIdObj.toString());
                continue;
            }

            log.info("start newSymbolToPublish {}", symbolIdObj.toString());
            MarketAddPO po = JsonUtil.defaultGson().fromJson(marketPoObject.toString(), MarketAddPO.class);
            marketAdd(po);
        }
    }
}
