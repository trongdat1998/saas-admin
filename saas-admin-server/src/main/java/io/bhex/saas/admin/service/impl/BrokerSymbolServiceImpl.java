package io.bhex.saas.admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.protobuf.TextFormat;
import io.bhex.base.account.*;
import io.bhex.base.bhadmin.*;
import io.bhex.base.bhadmin.GetSymbolPager;
import io.bhex.base.margin.*;
import io.bhex.base.proto.BaseRequest;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.*;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.broker.common.util.JsonUtil;
import io.bhex.broker.grpc.admin.AdminSymbolServiceGrpc;
import io.bhex.broker.grpc.admin.SymbolAgencyReply;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.controller.dto.BrokerSymbolDTO;
import io.bhex.saas.admin.controller.dto.SymbolRecordDTO;
import io.bhex.saas.admin.controller.param.AuditPO;
import io.bhex.saas.admin.controller.param.BatchAllowSymbolPO;
import io.bhex.saas.admin.dao.ExchangeSymbolMapper;
import io.bhex.saas.admin.dao.SymbolApplyRecordMapper;
import io.bhex.saas.admin.enums.EventTypeEnum;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.ExchangeSwapClient;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.MarginClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.grpc.client.impl.OrderClient;
import io.bhex.saas.admin.http.param.MarketAddPO;
import io.bhex.saas.admin.http.param.MarketRemovePO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;
import io.bhex.saas.admin.model.*;
import io.bhex.saas.admin.service.*;
import io.bhex.saas.admin.util.SymbolUtil;
import io.grpc.Channel;
import io.grpc.Deadline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/5 上午10:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BrokerSymbolServiceImpl implements BrokerSymbolService {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerSymbolClient brokerSymbolClient;
    @Autowired
    private ExchangeSwapClient exchangeSwapClient;
    @Autowired
    private ExchangeSymbolMapper exchangeSymbolMapper;
    @Autowired
    private ExchangeInfoService exchangeInfoService;
    @Autowired
    private BrokerTokenService brokerTokenService;
    @Autowired
    private SymbolApplyRecordMapper applyRecordMapper;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;
    @Autowired
    private EventLogService eventLogService;
    @Autowired
    private SymbolApplyRecordService symbolApplyRecordService;
    @Autowired
    private BhOrgClient bhOrgClient;
    @Autowired
    private SymbolTransferService symbolTransferService;
    @Autowired
    private OrderClient orderClient;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private MarginClient marginClient;

    private AdminSymbolServiceGrpc.AdminSymbolServiceBlockingStub getSymbolStub(long brokerId) {
        return AdminSymbolServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(brokerId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }
    private static Map<String, Boolean> privateTokenMap = new HashMap<>();

    private static Map<String, Map<String, Object>> delayMap = Maps.newHashMap();

    @Override
    public void copySymbolQuote(Long fromExchangeId, Long toExchangeId, String symbolId) {
        if (delayMap.containsKey(symbolId)) {
            return;
        }
        MarketRemovePO marketRemovePO = MarketRemovePO.builder()
                .exchangeId(fromExchangeId)
                .symbolId(symbolId)
                .remark("")
                .build();
        ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(fromExchangeId);
        ExchangeResultRes<Map<String, Long>> result = FeignConfig.getExchangeGatewayClient(instanceDetail.getGatewayUrl()).marketRemove(marketRemovePO);
        log.info("market remove request:{} result: {}", marketRemovePO, result);
        if (result.getStatus() != 200) {
            log.error("market remove request:{} result: {}", marketRemovePO, result);
            throw new BizException(ErrorCode.ERROR, result.getErr());
        }

        Map<String, Object> item = Maps.newHashMap();
        item.put("op", "copy");
        item.put("fromExchangeId", fromExchangeId);
        item.put("toExchangeId", toExchangeId);
        item.put("symbolId", symbolId);
        item.put("created", System.currentTimeMillis());
        delayMap.put(symbolId, item);

        //brokerSymbolClient.copyQuoteData(fromExchangeId, toExchangeId, symbolId);
    }

    @Scheduled(cron = "1/30 * * * * ?")
    public void eventExecutor() {
        Set<String> symbols = delayMap.keySet();
        for (String symbolId : symbols) {
            Map<String, Object> item = delayMap.get(symbolId);
            if (System.currentTimeMillis() - MapUtils.getLong(item, "created") < 60_000) {
                continue;
            }
            if (MapUtils.getString(item, "op").equals("copy")) {
                brokerSymbolClient.copyQuoteData(MapUtils.getLong(item, "fromExchangeId"), MapUtils.getLong(item, "toExchangeId"), symbolId);
                log.info("copyQuoteData suc {}", JsonUtil.defaultGson().toJson(item));
            } else if (MapUtils.getString(item, "op").equals("delete")) {
                brokerSymbolClient.deleteQuoteData(MapUtils.getLong(item, "exchangeId"), symbolId);
                log.info("deleteQuoteData suc {}", JsonUtil.defaultGson().toJson(item));
            }

            delayMap.remove(symbolId);

        }
    }


    @Override
    public void deleteQuoteData(Long exchangeId, String symbolId) {
        if (delayMap.containsKey(symbolId)) {
            return;
        }
        MarketRemovePO marketRemovePO = MarketRemovePO.builder()
                    .exchangeId(exchangeId)
                    .symbolId(symbolId)
                    .remark("")
                    .build();
        ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(exchangeId);
        ExchangeResultRes<Map<String, Long>> result = FeignConfig.getExchangeGatewayClient(instanceDetail.getGatewayUrl()).marketRemove(marketRemovePO);
        log.info("market remove request:{} result: {}", marketRemovePO, result);
        if (result.getStatus() != 200) {
            log.error("market remove request:{} result: {}", marketRemovePO, result);
            throw new BizException(ErrorCode.ERROR, result.getErr());
        }

        Map<String, Object> item = Maps.newHashMap();
        item.put("op", "delete");
        item.put("exchangeId", exchangeId);
        item.put("symbolId", symbolId);
        item.put("created", System.currentTimeMillis());
        delayMap.put(symbolId, item);

        //brokerSymbolClient.deleteQuoteData(exchangeId, symbolId);
    }



    @Override
    public SymbolDetail getSymbolBySymbolId(String symbolId) {
        try {
            return brokerSymbolClient.getBhSymbol(symbolId);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void syncExchangeTokens(Long exchangeId) {
        Integer current = 1;
        Integer pageSize = 23;
        Boolean isOver = false;

        while (!isOver) {
            SymbolList reply = brokerSymbolClient.getAllSymbols();
            List<SymbolDetail> tokenDetails = reply.getSymbolDetailsList();
            if (CollectionUtils.isEmpty(tokenDetails)) {
                break;
            }

            for (SymbolDetail symbolDetail : tokenDetails) {
                ExchangeSymbol exchangeSymbol = exchangeSymbolMapper.getByExchangeIdAndSymbol(exchangeId, symbolDetail.getSymbolId());
                if (exchangeSymbol == null) {
                    exchangeSymbol = new ExchangeSymbol();
                    BeanUtils.copyProperties(symbolDetail, exchangeSymbol);

                    exchangeSymbol.setCategory(new Long(symbolDetail.getCategory()).intValue());
                    exchangeSymbol.setExchangeId(exchangeId);
                    exchangeSymbol.setUnderlyingId(symbolDetail.getUnderlyingId());
                    exchangeSymbol.setStatus(0);
                    exchangeSymbol.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    exchangeSymbol.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    log.info("token:{}", exchangeSymbol);
                    exchangeSymbolMapper.insert(exchangeSymbol);
                }
            }
            isOver = pageSize > tokenDetails.size();
        }
    }

    @Override
    public SymbolApplyRecordList listApplyRecords(GetSymbolPager request) {
        Example example = new Example(SymbolApplyRecord.class);
        Example.Criteria criteria = example.createCriteria();
        if (request.getBrokerId() != -1) {
            criteria.andEqualTo("brokerId", request.getBrokerId());
        }
        if (request.getState() != -1) {
            criteria.andEqualTo("state", request.getState());
        }
        if (StringUtils.isNotEmpty(request.getSymbol())) {
            Example.Criteria tokenCriteria = example.createCriteria();
            tokenCriteria.andLike("symbolId", "%" + request.getSymbol().toUpperCase() + "%")
                    .orLike("baseTokenId", "%" + request.getSymbol().toUpperCase() + "%")
                    .orLike("quoteTokenId", "%" + request.getSymbol().toUpperCase() + "%");
            example.and(tokenCriteria);
        }
        example.orderBy("updateAt").desc();
        Page page = PageHelper.startPage(request.getStart(), request.getSize());
        List<SymbolApplyObj> resultList = new ArrayList<>();
        List<SymbolApplyRecord> symbolRecordList = applyRecordMapper.selectByExample(example);
        if (Objects.nonNull(symbolRecordList)) {
            resultList = symbolRecordList
                    .stream()
                    .map(r -> toProtoObj(r))
                    .collect(Collectors.toList());
        }
        return SymbolApplyRecordList.newBuilder()
                .addAllSymbolRecord(resultList)
                .setTotal((int) page.getTotal())
                .build();
    }

    private SymbolApplyObj toProtoObj(SymbolApplyRecord record) {
        SymbolApplyObj applyObj = record.toProtoObj();
        EventLog eventLog = eventLogService.getUnSuccessEvent(record.getBrokerId(), record.getId());
        if (eventLog != null) {
            applyObj = applyObj.toBuilder().setUpdateStatus(eventLog.getStatus()).build();
        }
        return applyObj;
    }

    @Override
    public PaginationVO<SymbolRecordDTO> applicationList(long brokerId, Integer current, Integer pageSize, Integer state, String symbol) {
        GetSymbolPager pager = GetSymbolPager.newBuilder()
                .setStart(current)
                .setSize(pageSize)
                .setState(state)
                .setBrokerId(brokerId <= 0 ? -1 : brokerId)
                .setSymbol(Strings.nullToEmpty(symbol))
                .build();

        SymbolApplyRecordList symbolRecordList = listApplyRecords(pager);
        PaginationVO<SymbolRecordDTO> symbolRecordDTOPaginationVO = new PaginationVO<>();
        List<SymbolRecordDTO> resultList = symbolRecordList.getSymbolRecordList().stream()
                .map(SymbolRecordDTO::parseSymbolRecord)
                .collect(Collectors.toList());
        resultList.forEach(detail -> {
            if (!privateTokenMap.containsKey(detail.getBaseToken())) {
                TokenDetailInfo tokenDetailInfo = brokerTokenService.getBhTokenInfo(detail.getBaseToken());
                boolean privateToken = tokenDetailInfo.getPrivateTokenBrokerId() > 0;
                privateTokenMap.put(detail.getBaseToken(), privateToken);
            }
            if (!privateTokenMap.containsKey(detail.getQuoteToken())) {
                TokenDetailInfo tokenDetailInfo = brokerTokenService.getBhTokenInfo(detail.getQuoteToken());
                boolean privateToken = tokenDetailInfo.getPrivateTokenBrokerId() > 0;
                privateTokenMap.put(detail.getQuoteToken(), privateToken);
            }
            detail.setIsPrivateSymbol(isPrivateSymbol(detail.getBaseToken(), detail.getQuoteToken()));
            EventLog eventLog = eventLogService.getUnSuccessEvent(detail.getBrokerId(), detail.getId());
            if (eventLog != null) {
                detail.setUpdateStatus(eventLog.getStatus());
            }
        });
        symbolRecordDTOPaginationVO.setList(resultList);

        Map<Long, String> brokerNameMap = brokerService.queryBrokerName();
        for (SymbolRecordDTO symbolRecordDTO : symbolRecordDTOPaginationVO.getList()) {
            symbolRecordDTO.setOrgName(brokerNameMap.getOrDefault(symbolRecordDTO.getBrokerId(), ""));
        }

        symbolRecordDTOPaginationVO.setCurrent(current);
        symbolRecordDTOPaginationVO.setPageSize(Math.min(symbolRecordList.getSymbolRecordCount(), pageSize));
        symbolRecordDTOPaginationVO.setTotal(symbolRecordList.getTotal());
        return symbolRecordDTOPaginationVO;
    }

    private boolean isPrivateSymbol(String baseTokenId, String quoteTokenId) {
        if (!privateTokenMap.containsKey(baseTokenId)) {
            TokenDetailInfo tokenDetailInfo = brokerTokenService.getBhTokenInfo(baseTokenId);
            boolean privateToken = tokenDetailInfo.getPrivateTokenBrokerId() > 0;
            privateTokenMap.put(baseTokenId, privateToken);
        }
        if (!privateTokenMap.containsKey(quoteTokenId)) {
            TokenDetailInfo tokenDetailInfo = brokerTokenService.getBhTokenInfo(quoteTokenId);
            boolean privateToken = tokenDetailInfo.getPrivateTokenBrokerId() > 0;
            privateTokenMap.put(quoteTokenId, privateToken);
        }
        return privateTokenMap.get(baseTokenId) || privateTokenMap.get(quoteTokenId);
    }

//    public PaginationVO<BrokerSymbolDTO> query(Long brokerId, Integer current, Integer pageSize, Integer category, boolean agent, String symbol) {
//
//
//        QuerySymbolReply reply = brokerSymbolClient.queryBrokerSymbols(brokerId, current, pageSize, category, symbol, null);
//        List<io.bhex.broker.grpc.admin.SymbolDetail> brokerSymbols = reply.getSymbolDetailsList();
//
//        List<String> symbolIds = brokerSymbols.stream().map(s -> s.getSymbolId()).collect(Collectors.toList());
//        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(brokerId);
//        if (exchangeReply == null || exchangeReply.getExchangeId() == 0) {
//            throw new BizException("no exchange");
//        }
//        List<ExchangeSymbolDetail> exchangeSymbolDetails = brokerSymbolClient.queryExchangeSymbols(exchangeReply.getExchangeId(), symbolIds);
//
//        QueryUpdatingSymbolsResult updatingSymbolsResult = eventLogService.getUpdatingSymbols(brokerId, symbolIds);
//
//        List<BrokerSymbolDTO> list = new ArrayList<>();
//        for (io.bhex.broker.grpc.admin.SymbolDetail detail : brokerSymbols) {
//            BrokerSymbolDTO dto = new BrokerSymbolDTO();
//            BeanUtils.copyProperties(detail, dto);
//            dto.setCreatedAt(detail.getCreated());
//
//            dto.setUpdateStatus(updatingSymbolsResult.getResultMap().getOrDefault(detail.getSymbolId(), 0));
//            dto.setShowStatus(true);
//            dto.setPublished(true);
//            for (ExchangeSymbolDetail exchangeSymbolDetail : exchangeSymbolDetails) {
//                if (detail.getSymbolId().equals(exchangeSymbolDetail.getSymbolId())) {
//                    dto.setSaasAllowTradeStatus(exchangeSymbolDetail.getSaasAllowTradeStatus());
//                    dto.setIsPrivateSymbol(exchangeSymbolDetail.getIsPrivate());
//                    dto.setIsAggregate(exchangeSymbolDetail.getIsAggregate());
//                    dto.setIsBaas(exchangeSymbolDetail.getIsBaas());
//                    dto.setIsTest(exchangeSymbolDetail.getIsTest());
//                    dto.setApplyBrokerId(exchangeSymbolDetail.getApplyBrokerId());
//                    dto.setIsMainstream(exchangeSymbolDetail.getIsMainstream());
//                    break;
//                }
//            }
//            list.add(dto);
//        }
//
//        PaginationVO<BrokerSymbolDTO> vo = new PaginationVO<>();
//        vo.setTotal(reply.getTotal());
//        vo.setCurrent(current);
//        vo.setPageSize(pageSize);
//        vo.setList(list);
//        return vo;
//    }

    @Override
    public PaginationVO<BrokerSymbolDTO> query(Long brokerId, Integer current, Integer pageSize, Integer category, boolean agent, String symbol) {
        long st = System.currentTimeMillis();
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(brokerId);
        if (exchangeReply == null || exchangeReply.getExchangeId() == 0) {
            throw new BizException("no exchange");
        }
        QueryExchangeSymbolsReply reply = brokerSymbolClient.queryExchangeSymbols(exchangeReply.getExchangeId(), current, pageSize, agent, category, symbol);
        List<ExchangeSymbolDetail> exchangeSymbolDetails = reply.getExchangeSymbolDetailsList();
        log.info("queryExchangeSymbols : {}", System.currentTimeMillis() - st);
        List<String> symbolIds = exchangeSymbolDetails.stream().map(s -> s.getSymbolId()).collect(Collectors.toList());

        List<io.bhex.broker.grpc.admin.SymbolDetail> brokerSymbols = brokerSymbolClient.queryBrokerSymbols(brokerId,
                1, 1000, category, null, symbolIds).getSymbolDetailsList();

        QueryUpdatingSymbolsResult updatingSymbolsResult = eventLogService.getUpdatingSymbols(brokerId, symbolIds);

        long t0 = System.currentTimeMillis();

        List<BrokerSymbolDTO> list = new ArrayList<>();
        for (ExchangeSymbolDetail detail : exchangeSymbolDetails) {
            BrokerSymbolDTO dto = new BrokerSymbolDTO();
            BeanUtils.copyProperties(detail, dto);

            dto.setUpdateStatus(updatingSymbolsResult.getResultMap().getOrDefault(detail.getSymbolId(), 0));

            Optional<io.bhex.broker.grpc.admin.SymbolDetail> brokerSymbolOptional = brokerSymbols.stream()
                    .filter(s -> s.getSymbolId().equals(detail.getSymbolId()))
                    .findFirst();
            if (category == TokenCategory.OPTION_CATEGORY_VALUE) { //期权提前进入到 broker.tb_symbol了，特殊处理
                dto.setShowStatus(detail.getSaasAllowTradeStatus());
                dto.setPublished(detail.getSaasAllowTradeStatus());
                dto.setCreatedAt(detail.getCreatedAt());
            } else if (brokerSymbolOptional.isPresent()) {
                dto.setShowStatus(true);
                dto.setPublished(brokerSymbolOptional.get().getPublished());
                dto.setCreatedAt(brokerSymbolOptional.get().getCreated());
            } else {
                dto.setShowStatus(false);
                dto.setPublished(false);
                dto.setCreatedAt(detail.getCreatedAt());
            }

            dto.setMinTradeQuantity(new BigDecimal(detail.getMinTradeQuantity().getStr()));
            dto.setMinTradeAmount(new BigDecimal(detail.getMinTradeAmount().getStr()));
            dto.setMinPricePrecision(new BigDecimal(detail.getMinPricePrecision().getStr()));
            dto.setBasePrecision(new BigDecimal(detail.getBasePrecision().getStr()));
            dto.setQuotePrecision(new BigDecimal(detail.getQuotePrecision().getStr()));


            dto.setSaasAllowTradeStatus(detail.getSaasAllowTradeStatus());
            dto.setIsPrivateSymbol(detail.getIsPrivate());
            dto.setIsAggregate(detail.getIsAggregate());
            dto.setIsBaas(detail.getIsBaas());
            dto.setIsTest(detail.getIsTest());
            dto.setApplyBrokerId(detail.getApplyBrokerId());
            dto.setIsMainstream(detail.getIsMainstream());

            list.add(dto);
        }
        log.info("aggerate consume : {}", System.currentTimeMillis() - t0);

        PaginationVO<BrokerSymbolDTO> vo = new PaginationVO<>();
        vo.setTotal(reply.getTotal());
        vo.setCurrent(current);
        vo.setPageSize(pageSize);
        BeanUtils.copyProperties(reply, vo);
        vo.setList(list);
        return vo;

    }

    @Override
    public Combo2<Boolean, String> publishExchangeSymbol(Long exchangeId, String symbolId, String service) {

        PublishExchangeSymbolReply reply = brokerSymbolClient.publishExchangeSymbol(exchangeId,
                System.getProperty("matchEngine") != null ? System.getProperty("matchEngine") : "match-engine.bhop", service,
                7040, symbolId, YesNoEnum.YES_VALUE);

        return new Combo2(reply.getResult(), reply.getMessage());
    }

    private boolean closeExchangeSymbol(Long exchangeId, String symbolId, String service) {

        PublishExchangeSymbolReply reply = brokerSymbolClient.publishExchangeSymbol(exchangeId,
                System.getProperty("matchEngine") != null ? System.getProperty("matchEngine") : "match-engine.bhop", service,
                7040, symbolId, YesNoEnum.NO_VALUE);

        return reply.getResult();
    }
    /**
     * 更新状态，确认是否展示在交易所的币种中
     *
     * @param exchangeId
     * @param showInExchange
     * @return
     */
    @Override
    public boolean updateShowStatusInExchange(Long exchangeId, String symbolId, boolean showInExchange) {
        ExchangeSymbol exchangeSymbol = exchangeSymbolMapper.getByExchangeIdAndSymbol(exchangeId, symbolId);

        if (exchangeSymbol == null && showInExchange) {
            SymbolDetail symbolDetail = brokerSymbolClient.getBhSymbol(symbolId);
            log.info("symbolDetail : {}", TextFormat.shortDebugString(symbolDetail));
            exchangeSymbol = new ExchangeSymbol();
            BeanUtils.copyProperties(symbolDetail, exchangeSymbol);

            exchangeSymbol.setExchangeId(exchangeId);
            exchangeSymbol.setCategory(new Long(symbolDetail.getCategory()).intValue());
            exchangeSymbol.setStatus(showInExchange ? 1 : 0);
            exchangeSymbol.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            exchangeSymbol.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            log.info("exchangeSymbol:{}", exchangeSymbol);
            exchangeSymbolMapper.insert(exchangeSymbol);
        } else {
            exchangeSymbolMapper.updateStatus(exchangeId, symbolId, showInExchange ? 1 : 0);
        }

        return true;
    }

    @Override
    public AllowExchangeSymbolTradeReply exchangeAllowTrade(long exchangeId, String symbolId, YesNoEnum yesNoEnum) {
        AllowExchangeSymbolTradeReply reply = brokerSymbolClient.exchangeAllowTrade(exchangeId, symbolId, yesNoEnum);
        return reply;
    }



    @Override
    public QueryExchangeSymbolsReply queryExchangeSymbols(Long exchangeId, String symbolId, Integer current, Integer pageSize, boolean myAgentSymbol, Integer category) {
        Combo2<Integer, List<ExchangeSymbolDetail>> combo2 = queryExSymbols(exchangeId, symbolId, current, pageSize, myAgentSymbol, category);
        if (combo2.getV1() == 0) {
            return QueryExchangeSymbolsReply.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize)
                .setTotal(0)
                .build();
        }

        QueryExchangeSymbolsReply.Builder builder = QueryExchangeSymbolsReply.newBuilder();
        builder.setTotal(combo2.getV1());
        builder.setCurrent(current);
        builder.setPageSize(pageSize);

        List<ExchangeSymbolDetail> symbolDetails = combo2.getV2();
        builder.addAllExchangeSymbolDetails(symbolDetails);
        return builder.build();
    }

    @Override
    public QueryAgentFuturesReply queryAgentFutures(Long exchangeId, String symbolId, Integer current, Integer pageSize) {
        Combo2<Integer, List<ExchangeSymbolDetail>> combo2 = queryExSymbols(exchangeId, symbolId, current, pageSize, true, 4);
        if (combo2.getV1() == 0) {
            return QueryAgentFuturesReply.newBuilder()
                    .setCurrent(current)
                    .setPageSize(pageSize)
                    .setTotal(0)
                    .build();
        }

        // 获取的币对列表要与本交易所创建的去重
        List<String> symbolIds = queryOwnerFutures(exchangeId).stream().map(ExchangeSymbolDetail::getSymbolId).collect(Collectors.toList());
        List<ExchangeSymbolDetail> symbolDetails = combo2.getV2().stream().filter(f -> !symbolIds.contains(f.getSymbolId())).collect(Collectors.toList());


        QueryAgentFuturesReply.Builder builder = QueryAgentFuturesReply.newBuilder();
        builder.setTotal(combo2.getV1());
        builder.setCurrent(current);
        builder.setPageSize(pageSize);

        builder.addAllExchangeSymbolDetails(symbolDetails);
        return builder.build();
    }

    private List<ExchangeSymbolDetail> queryOwnerFutures(Long exchangeId) {
        QueryOwenFuturesRequest request = QueryOwenFuturesRequest.newBuilder()
                .setExchangeId(exchangeId)
                .build();
        QueryOwenFuturesReply reply = grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME).queryOwenFutures(request);
        return reply.getExchangeSymbolDetailsList();
    }

    @Override
    public QueryBrokerExchangeSymbolsReply queryBrokerExchangeSymbols(Long exchangeId, Integer current, Integer pageSize, Integer category) {
        Combo2<Integer, List<ExchangeSymbolDetail>> combo2 = queryExSymbols(exchangeId, null, current, pageSize, true, category);
        if (combo2.getV1() == 0) {
            return QueryBrokerExchangeSymbolsReply.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize)
                .setTotal(0)
                .build();
        }

        QueryBrokerExchangeSymbolsReply.Builder builder = QueryBrokerExchangeSymbolsReply.newBuilder();
        builder.setTotal(combo2.getV1());
        builder.setCurrent(current);
        builder.setPageSize(pageSize);

        List<ExchangeSymbolDetail> symbolDetails = combo2.getV2();
        builder.addAllExchangeSymbolDetails(symbolDetails);
        return builder.build();
    }

    @Override
    public int auditSymbolRecord(long brokerId, AuditPO auditPO) {
        if (eventLogService.hasUndoneTask(brokerId, auditPO.getId())) {
            return 0;
        }
        AuditSymbolApplyRequest request = AuditSymbolApplyRequest.newBuilder()
                .setCurState(auditPO.getCurState())
                .setUpdatedState(auditPO.getToState())
                .setId(auditPO.getId())
                .setReason(auditPO.getSymbolId())
                .setSymbolId(auditPO.getSymbolId())
                .setBasePrecision(DecimalUtil.fromBigDecimal(auditPO.getBasePrecision()))
                .setQuotePrecision(DecimalUtil.fromBigDecimal(auditPO.getQuotePrecision()))
                .setMergeDigitDepth(auditPO.getMergeDigitDepth())
                .setBaseToken(auditPO.getBaseToken())
                .setQuoteToken(auditPO.getQuoteToken())
                .setMinPricePrecision(DecimalUtil.fromBigDecimal(auditPO.getMinPricePrecision()))
                .setMinTradeAmt(DecimalUtil.fromBigDecimal(auditPO.getMinTradeAmt()))
                .setMinTradeQuantity(DecimalUtil.fromBigDecimal(auditPO.getMinTradeQuantity()))
                .build();
        ApplySymbolResult result = auditApplySymbol(request);
        if (result.getRes() == -1) {
            throw new BizException(ErrorCode.SYMBOL_ALREADY_EXIST);
        }


        EventLog eventLog = eventLogService.addEvent(result.getSymbolRecord().getBrokerId(),
                result.getSymbolRecord().getId(), auditPO.getSymbolId(), EventTypeEnum.AUDIT_SYMBOL_SUCCESS);
        eventLogService.doEvent(eventLog);
//        auditSymbolFull(result.getSymbolRecord().getId(), eventLog.getId());
        return 0;
    }

    private ApplySymbolResult auditApplySymbol(AuditSymbolApplyRequest request) {
        SymbolApplyRecord symbolRecord = applyRecordMapper.selectByPrimaryKey(request.getId());
        if (Objects.nonNull(symbolRecord)) {
            if (symbolRecord.getState().equals(request.getCurState())) {
                symbolRecord.setState(request.getUpdatedState());
                symbolRecord.setSymbolId(request.getSymbolId());
                symbolRecord.setBaseTokenId(request.getBaseToken());
                symbolRecord.setQuoteTokenId(request.getQuoteToken());
                symbolRecord.setMinPricePrecision(DecimalUtil.toBigDecimal(request.getMinPricePrecision()));
                symbolRecord.setBasePrecision(DecimalUtil.toBigDecimal(request.getBasePrecision()));
                symbolRecord.setQuotePrecision(DecimalUtil.toBigDecimal(request.getQuotePrecision()));
                symbolRecord.setMinTradeQuantity(DecimalUtil.toBigDecimal(request.getMinTradeQuantity()));
                symbolRecord.setMinTradeAmt(DecimalUtil.toBigDecimal(request.getMinTradeAmt()));
                symbolRecord.setMergeDigitDepth(request.getMergeDigitDepth());
                Example example = new Example(SymbolApplyRecord.class);
                example.createCriteria()
                        .andEqualTo("id", symbolRecord.getId())
                        .andEqualTo("state", request.getCurState());
                applyRecordMapper.updateByExampleSelective(symbolRecord, example);
            }
        }
        return ApplySymbolResult.newBuilder()
                .setRes(0)
                .setSymbolRecord(symbolRecord.toProtoObj())
                .build();
    }

    @Override
    public void changeSymbolBroker(Long brokerId, String symbolId, Long toBrokerId) {
        int res = symbolApplyRecordService.changeSymbolRecordBrokerId(brokerId, symbolId, toBrokerId);
        if (res == -1) {
            throw new BizException(ErrorCode.SYMBOL_MISSING);
        }
    }

    private Combo2<Integer, List<ExchangeSymbolDetail>> queryExSymbols(Long exchangeId, String symbolId, Integer current, Integer pageSize, boolean myAgentSymbol, Integer category) {
        List<Long> exchangeIds = new ArrayList<>();
        exchangeIds.add(exchangeId);
        Integer total = exchangeSymbolMapper.countExchangeSymbols(exchangeIds, symbolId, myAgentSymbol, category);
        if (total == 0) {
            return new Combo2<>(0, new ArrayList<>());
        }

        int start = (current - 1) * pageSize;
        if (start >= total) {
            return new Combo2<>(0, new ArrayList<>());
        }


        List<ExchangeSymbol> exchangeSymbols = exchangeSymbolMapper.queryExchangeSymbols(exchangeIds, symbolId, myAgentSymbol, category, start, pageSize);
        List<String> symbolIds = exchangeSymbols.stream()
            .map(exchangeSymbol -> exchangeSymbol.getSymbolId())
            .collect(Collectors.toList());


        List<ExchangeSymbolDetail> symbolDetails = brokerSymbolClient.queryExchangeSymbols(exchangeId, symbolIds);
        //log.info("symbolDeatils in bh:{}", symbolDetails);
//        List<ExchangeSymbolDetail>  symbolDetails = new ArrayList<>();
//        for(String symbol : symbolIds){
//            ExchangeSymbolDetail detail = exchangeSymbolClient.queryExchangeSymbol(exchangeId, symbol);
//            symbolDetails.add(detail);
//        }
        return new Combo2<>(total, symbolDetails);
    }

    @Override
    public boolean saasAllowTrade(Long exchangeId, String symbolId, boolean enabled) {
        return brokerSymbolClient.saasAllowTrade(exchangeId, symbolId, enabled);
    }




    //1. 修改 symbol_apply
    //2. bh-server.publishSymbol
    //3. bh-server.publishExchangeSymbol
    //4. gateway.marketAdd
    //5. broker.agencySymbol
    @Override
    public void auditSymbolFull(long applyId, long eventId) {
        SymbolApplyRecord applyRecord = applyRecordMapper.selectByPrimaryKey(applyId);

        if (applyRecord.getState() != ApplyStateEnum.ACCEPT.getState()) {
            eventLogService.eventEnd(eventId);
            return;
        }

        //bh.tb_symbol
        PublishSymbolRequest publishSymbolRequest = PublishSymbolRequest.newBuilder()
                .setSymbolId(applyRecord.getSymbolId())
                .setQuoteTokenId(applyRecord.getQuoteTokenId())
                .setBaseTokenId(applyRecord.getBaseTokenId())
                .setQuotePrecision(DecimalUtil.fromBigDecimal(applyRecord.getQuotePrecision()))
                .setBasePrecision(DecimalUtil.fromBigDecimal(applyRecord.getBasePrecision()))
                .setMinTradeQuantity(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeQuantity()))
                .setMinTradeAmt(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeAmt()))
                .setMinPricePrecision(DecimalUtil.fromBigDecimal(applyRecord.getMinPricePrecision()))
                .setDepthMerge(applyRecord.getMergeDigitDepth())
                .setApplyBrokerId(applyRecord.getBrokerId())
                .build();
        PublishSymbolReply publishSymbolReply = brokerSymbolClient.publishBhSymbol(publishSymbolRequest);
        //查询该币对是否开通了杠杆
        QueryMarginSymbolRequest request = QueryMarginSymbolRequest.newBuilder()
                .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(applyRecord.getBrokerId()).build())
                .setSymbolId(applyRecord.getSymbolId())
                .build();
        QueryMarginSymbolReply marginSymbolReply = marginClient.queryMarginSymbol(request);
        if(marginSymbolReply.getSymbolsCount()>0){
            MarginSymbol marginSymbol = marginSymbolReply.getSymbols(0);
            //杠杆配置过该币种，同步精度
            SetSymbolConfigRequest setSymbolConfigRequest = SetSymbolConfigRequest.newBuilder()
                    .setBaseRequest(BaseRequest.newBuilder().setOrganizationId(applyRecord.getBrokerId()).build())
                    .setExchangeId(applyRecord.getExchangeId())
                    .setSymbolId(applyRecord.getSymbolId())
                    .setAllowTrade(marginSymbol.getAllowTrade())
                    .setMinTradeQuantity(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeQuantity()))
                    .setMinTradeAmount(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeAmt()))
                    .setMinPricePrecision(DecimalUtil.fromBigDecimal(applyRecord.getMinPricePrecision()))
                    .setBasePrecision(DecimalUtil.fromBigDecimal(applyRecord.getBasePrecision()))
                    .setQuotePrecision(DecimalUtil.fromBigDecimal(applyRecord.getQuotePrecision()))
                    .build();
            SetSymbolConfigReply setSymbolConfigReply = marginClient.updateSymbolInfo(setSymbolConfigRequest);

        }
        publishBrokerSymbol(applyRecord.getBrokerId(), applyRecord.getSymbolId(), eventId);
    }

    @Override
    public void batchPublishBrokerSymbol(BatchAllowSymbolPO po) {
        List<EventLog> eventLogs = Lists.newArrayList();
        for (long broker : po.getBrokers()) {
            for (String symbol : po.getSymbols()) {
                EventLog eventLog = eventLogService.addEvent(broker, 0, symbol.replace("/", ""), EventTypeEnum.PUBLISH_BROKER_SYMBOL);
                eventLogs.add(eventLog);
            }
        }
        eventLogs.forEach(eventLog -> eventLogService.doEvent(eventLog));
        //写到事件中， 最终调用 publishBrokerSymbol
    }

    @Override
    public OpenSymbolResult publishBrokerSymbol(long brokerId, String symbolId, long eventId) {
        //bh.tb_exchange_symbol
        symbolId = symbolId.replaceAll("/", "");
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(brokerId);
        SymbolDetail symbolDetail = getSymbolBySymbolId(symbolId);
        if (symbolDetail == null || StringUtils.isEmpty(symbolDetail.getSymbolId())) {
            return OpenSymbolResult.newBuilder().setRes(1).setMessage("please.wait").build();
        }
        Combo2<String, String> combo2 = SymbolUtil.partitionName(symbolDetail.getQuoteTokenId(), symbolDetail.getCategory(), exchangeReply.getExchangeId());
        Combo2<Boolean, String> publishExchangeSymbolResult = publishExchangeSymbol(exchangeReply.getExchangeId(), symbolDetail.getSymbolId(), combo2.getV1());
        if (!publishExchangeSymbolResult.getV1()) {
            log.info("{} publishExchangeSymbol not suc", symbolDetail.getSymbolId());
            return OpenSymbolResult.newBuilder().setRes(1).setMessage("publish exchange symbol not success").build();
        }

        //bh.tb_exchange_symbol.allow_trade
        AllowExchangeSymbolTradeReply reply = exchangeAllowTrade(exchangeReply.getExchangeId(), symbolDetail.getSymbolId(), YesNoEnum.YES);
        if (!reply.getResult()) {
            log.info("{} exchangeAllowTrade not suc", symbolDetail.getSymbolId());
            return OpenSymbolResult.newBuilder().setRes(1).setMessage("AllowTrade not success").build();
        }

        //gateway.marketAdd & match
        List<Integer> dumpScales = SymbolUtil.convertDigitalMergedListToDumpScales(symbolDetail.getDigitMergeList());
        Boolean reverse = Objects.nonNull(symbolDetail) ? symbolDetail.getIsReverse() : false;
        MarketAddPO marketAddPO = MarketAddPO.builder()
                .partitionName(combo2.getV1())
                .topicName(combo2.getV2())
                .exchangeId(exchangeReply.getExchangeId())
                .symbolId(symbolDetail.getSymbolId())
                .symbolName(symbolDetail.getSymbolName())
                .remark("new symbol")
                .dumpScales(dumpScales)
                .isReverse(reverse)
                .firstReqTime(System.currentTimeMillis())
                .build();
        ExchangeInstanceDetail instanceDetail = exchangeInfoService.getInstanceInfoByExchangeId(marketAddPO.getExchangeId());
        ExchangeResultRes<Map<String, Long>> result = FeignConfig.getExchangeGatewayClient(instanceDetail.getGatewayUrl()).marketAdd(marketAddPO);
        log.info("market add request:{} result: {}", marketAddPO, result);
        if (result.getStatus() != 200 && result.getStatus() != 1304) { //成功插入或者已经存在都认为成功
            log.warn("marketAdd Error req:{} res:{}", marketAddPO, result);
            if (eventId == 0) {
                eventLogService.addEvent(brokerId, 0, symbolId.replace("/", ""), EventTypeEnum.PUBLISH_BROKER_SYMBOL);
            }
            return OpenSymbolResult.newBuilder().setRes(1).setMessage("MarketAdd not success").build();
        }

        //broker.tb_token
        brokerTokenService.initBrokerToken(brokerId, symbolDetail.getBaseTokenId(), (int)symbolDetail.getCategory());
        brokerTokenService.initBrokerToken(brokerId, symbolDetail.getQuoteTokenId(), (int)symbolDetail.getCategory());

        //broker.tb_symbol
        SymbolAgencyReply symbolAgencyReply = brokerSymbolClient.agencySymbol(exchangeReply.getExchangeId(),
                Lists.newArrayList(symbolDetail.getSymbolId()), brokerId);
        if (!symbolAgencyReply.getResult()) {
            log.error("symbolAgencyReply. broker:{} req:{} {}", brokerId, symbolDetail.getSymbolId(), TextFormat.shortDebugString(symbolAgencyReply));
            return OpenSymbolResult.newBuilder().setRes(1).setMessage("publish symbol not success").build();
        }
        if (eventId == 0) {
            EventLog eventLog = eventLogService.addEvent(brokerId, 0, symbolId.replace("/", ""), EventTypeEnum.PUBLISH_BROKER_SYMBOL);
            eventLogService.eventEnd(eventLog.getId());
        } else {
            eventLogService.eventEnd(eventId);
        }
        return OpenSymbolResult.newBuilder().setRes(0).build();
    }

    @Override
    public void batchCloseBrokerSymbol(BatchAllowSymbolPO po) {
        List<EventLog> eventLogs = Lists.newArrayList();
        for (long broker : po.getBrokers()) {
            for (String symbol : po.getSymbols()) {
                EventLog eventLog = eventLogService.addEvent(broker, 0, symbol, EventTypeEnum.CLOSE_BROKER_SYMBOL);
                eventLogs.add(eventLog);
            }
        }
        eventLogs.forEach(eventLog -> eventLogService.doEvent(eventLog));
        //写到事件中， 最终调用 closeSymbol
    }

    @Override
    public CloseSymbolResult closeSymbol(long brokerId, String symbolId, long eventId) {

        io.bhex.broker.grpc.admin.SymbolDetail symbolDetail = brokerSymbolClient.queryBrokerSymbolById(brokerId, symbolId);
        if (symbolDetail.getCategory() != TokenCategory.MAIN_CATEGORY_VALUE) {
            eventLogService.eventEnd(eventId);
            return CloseSymbolResult.newBuilder().setRes(0).build();
        }
        //本身转发到其它交易所，要先关闭转发关系
        SymbolMatchTransfer sourceTransfer = symbolTransferService.getSourceTransfer(brokerId, symbolId);
        if (sourceTransfer != null && sourceTransfer.getEnable() == 1) {
            return CloseSymbolResult.newBuilder().setRes(1).setMessage("symbol.transfer.not.close").build();
        }
        //关闭所有转发给我的转发关系
        SaveSymbolTransferReply closeTransferReply = symbolTransferService.closeTransferToMe(brokerId, symbolId);
        if (!closeTransferReply.getResult()) {
            log.error("close transfer to me error. {} {}", brokerId, symbolId);
            return CloseSymbolResult.newBuilder().setRes(1).setMessage("close.transfer.to.me.error").build();
        }

        //券商下币对
        brokerSymbolClient.closeBrokerSymbol(brokerId, symbolId);

        CancelSymbolOrdersReply reply = orderClient.cancelBrokerOrderNew(brokerId, symbolId);
        if (reply.getOpenOrdersCount() > 0) {
            log.info("cancel broker order not end. {},{} result:{}", brokerId, symbolId, TextFormat.shortDebugString(reply));
            if (eventId == 0) {
                eventLogService.addEvent(brokerId, 0, symbolId.replace("/", ""), EventTypeEnum.CLOSE_BROKER_SYMBOL);
            }
            return CloseSymbolResult.newBuilder().setRes(1).setMessage("please.wait").build();
        } else {
            log.info("cancel broker:{} {} orders end", brokerId, symbolId);
        }

        if (eventId == 0) {
            EventLog eventLog = eventLogService.addEvent(brokerId, 0, symbolId.replace("/", ""), EventTypeEnum.CLOSE_BROKER_SYMBOL);
            eventLogService.eventEnd(eventLog.getId());
        } else {
            eventLogService.eventEnd(eventId);
        }
        return CloseSymbolResult.newBuilder().setRes(0).build();
    }
}
