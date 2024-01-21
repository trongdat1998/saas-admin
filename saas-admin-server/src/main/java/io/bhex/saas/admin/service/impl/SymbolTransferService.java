package io.bhex.saas.admin.service.impl;

import com.google.protobuf.TextFormat;
import io.bhex.base.account.ConfigMatchTransferReply;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.bhadmin.*;
import io.bhex.base.exadmin.*;
import io.bhex.base.token.SymbolDetail;
import io.bhex.saas.admin.dao.SymbolMatchTransferMapper;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.SymbolMatchTransfer;
import io.bhex.saas.admin.service.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ProjectName: exchange
 * @Package: io.bhex.ex.admingrpc.service
 * @Author: ming.xu
 * @CreateDate: 22/11/2018 4:30 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class SymbolTransferService {

    @Autowired
    private SymbolMatchTransferMapper symbolMatchTransferMapper;
    @Autowired
    private BrokerSymbolClient brokerSymbolClient;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private BhOrgClient bhOrgClient;

    public ListSymbolTransferReply listSymbolTransferBySymbolIds(ListSymbolTransferRequest request) {
        Example example = new Example(SymbolMatchTransfer.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sourceBrokerId", request.getSourceBrokerId());
        criteria.andIn("symbolId", request.getSymbolIdsList());
        List<SymbolMatchTransfer> symbolMatchTransfers = symbolMatchTransferMapper.selectByExample(example);

        ListSymbolTransferReply.Builder builder = ListSymbolTransferReply.newBuilder();
        List<SymbolTransferInfo> infoList = symbolMatchTransfers.stream().map(transfer -> {
            SymbolTransferInfo.Builder info = SymbolTransferInfo.newBuilder();
            BeanUtils.copyProperties(transfer, info);
            return info.build();
        }).collect(Collectors.toList());
        builder.addAllSymbolTransferInfo(infoList);
        return builder.build();
    }

    public ListSymbolMatchTransferReply listSymbolMatchTransferByMatchExchangeId(ListSymbolMatchTransferByMatchExchangeIdRequest request) {
        Example example = new Example(SymbolMatchTransfer.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("matchExchangeId", request.getMatchExchangeId());
        criteria.andEqualTo("symbolId", request.getSymbolId());
        List<SymbolMatchTransfer> symbolMatchTransfers = symbolMatchTransferMapper.selectByExample(example);

        ListSymbolMatchTransferReply.Builder builder = ListSymbolMatchTransferReply.newBuilder();
        List<SymbolMatchTransferInfo> infoList = symbolMatchTransfers.stream().map(transfer -> {
            SymbolMatchTransferInfo.Builder info = SymbolMatchTransferInfo.newBuilder();
            BeanUtils.copyProperties(transfer, info);
            return info.build();
        }).collect(Collectors.toList());
        builder.addAllSymbolMatchTransferInfoList(infoList);
        return builder.build();
    }


    public SaveSymbolTransferReply saveSymbolMatchTransfer(SaveSymbolTransferRequest request) {
        ExchangeReply sourceExchange = bhOrgClient.findExchangeByBrokerId(request.getSourceBrokerId());
//        if (sourceExchange == null) {
//            log.error("{} not trusted broker.", request.getSourceBrokerId());
//            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("not trusted broker").build();
//        }
        //check 1. symbolid是否存在 symbol的上线状态 上线时间
        //      2. 券商状态
        //      3. 如果此币的matchBrokerid本身就是转发 则不允许转发了
        //      4. 券商的symbol也要打开状态
        Broker matchBroker = brokerService.getBrokerByBrokerName(request.getMatchBrokerName());
        if (matchBroker == null || !matchBroker.getEnabled()) {
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("org.not.exist").build();
        }
        if (request.getSourceBrokerId() == matchBroker.getBrokerId()) {
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage(getBhMatchTransferError(ConfigMatchTransferReply.ReplyCode.SOURCE_SYMBOL_HAS_BEEN_MATCHED)).build();
        }
        if (getTargetTransfer(request.getSourceBrokerId(), request.getSymbolId())  != null) {
            log.info("self is targetTransfer. cant trasfer to other. {}", TextFormat.shortDebugString(request));
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("transfer error").build();
        }
        if (getSourceTransfer(matchBroker.getBrokerId(), request.getSymbolId()) != null) { //请求的撮合交易所本身就是转发的 不能再二次转发
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("org.transfer.error").build();
        }

        SymbolDetail symbolDetail = brokerSymbolClient.getBhSymbol(request.getSymbolId());
        if (symbolDetail == null || !symbolDetail.getPublished()) {
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("symbol.disabled").build();
        }
//        if (symbolDetail.getOnlineTime() > 0 && symbolDetail.getOnlineTime() > System.currentTimeMillis()) {
//            log.info("not opened for transfer. onlineTime:{}", new Date(symbolDetail.getOnlineTime()), TextFormat.shortDebugString(request));
//            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("symbol.disabled").build();
//        }

        io.bhex.broker.grpc.admin.SymbolDetail brokerSymbol = brokerSymbolClient.queryBrokerSymbolById(matchBroker.getBrokerId(), request.getSymbolId());
        if (!brokerSymbol.getPublished()) {
            log.warn("source broker:{} {} closed.", matchBroker.getBrokerId(), request.getSymbolId());
            return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage("symbol.target.ex.disabled").build();
        }


        ExchangeReply targetExchange = bhOrgClient.findExchangeByBrokerId(matchBroker.getBrokerId());
        if (sourceExchange.getExchangeId() != targetExchange.getExchangeId()) {
            ConfigMatchTransferReply transferReply = bhOrgClient.configMatchTransfer(sourceExchange.getExchangeId(), targetExchange.getExchangeId(), request.getSymbolId(),
                    request.getEnable() == 1, (int)symbolDetail.getCategory());
            log.info("{}", TextFormat.shortDebugString(transferReply));
            if (transferReply.getCode() != ConfigMatchTransferReply.ReplyCode.SUCCESS) {
                return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage(getBhMatchTransferError(transferReply.getCode())).build();
            }
        }

        SymbolMatchTransfer transfer = getSourceTransfer(request.getSourceBrokerId(), request.getSymbolId());
        Boolean isAdd;
        if (null != transfer) {
            isAdd = false;
        } else {
            isAdd = true;
            transfer = new SymbolMatchTransfer();
            transfer.setCategory((int)symbolDetail.getCategory());
            transfer.setCreatedAt(System.currentTimeMillis());
            transfer.setSourceExchangeId(sourceExchange.getExchangeId());
            transfer.setSourceBrokerId(request.getSourceBrokerId());
            Broker sourceBroker = brokerService.getBrokerByBrokerId(request.getSourceBrokerId());
            transfer.setSourceBrokerName(sourceBroker.getName());
            transfer.setSymbolId(request.getSymbolId());
        }
        transfer.setCategory((int)symbolDetail.getCategory());
        transfer.setMatchBrokerId(matchBroker.getBrokerId());
        transfer.setMatchBrokerName(request.getMatchBrokerName());
        transfer.setMatchExchangeId(targetExchange.getExchangeId());
        transfer.setMatchExchangeName(targetExchange.getExchangeName());
        transfer.setEnable(request.getEnable());
        transfer.setRemark(request.getRemark());
        transfer.setUpdatedAt(System.currentTimeMillis());

        SaveSymbolTransferReply.Builder builder = SaveSymbolTransferReply.newBuilder();
        Boolean isOk;
        if (isAdd) {
            isOk = symbolMatchTransferMapper.insert(transfer) > 0 ? true: false;
        } else {
            isOk = symbolMatchTransferMapper.updateByPrimaryKey(transfer) > 0 ? true: false;
        }
        return builder.setResult(isOk).build();
    }

    public SymbolMatchTransferInfo getSymbolMatchTransferInfo(GetSymbolMatchTransferInfoRequest request) {
        SymbolMatchTransfer symbolMatchTransfer = getSourceTransfer(request.getExchangeId(), request.getSymbolId());

        SymbolMatchTransferInfo.Builder builder = SymbolMatchTransferInfo.newBuilder();
        if (Objects.nonNull(symbolMatchTransfer)) {
            BeanUtils.copyProperties(symbolMatchTransfer, builder);
        } else {
            log.error("Symbol Match Transfer Info is NULL: exchangeId => {}, symbolId => {}.", request.getExchangeId(), request.getSymbolId());
        }

        return builder.build();
    }

    public SymbolMatchTransfer getSourceTransfer(Long sourceBrokerId, String symbolId) {
        Example example = new Example(SymbolMatchTransfer.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sourceBrokerId", sourceBrokerId);
        criteria.andEqualTo("symbolId", symbolId);
        return symbolMatchTransferMapper.selectOneByExample(example);
    }

    public SymbolMatchTransfer getTargetTransfer(Long matchBrokerId, String symbolId) {
        Example example = new Example(SymbolMatchTransfer.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("matchBrokerId", matchBrokerId);
        criteria.andEqualTo("symbolId", symbolId);
        return symbolMatchTransferMapper.selectOneByExample(example);
    }

    public SaveSymbolTransferReply closeSymbolTransfer(CloseSymbolTransferRequest request) {
        SaveSymbolTransferReply.Builder builder = SaveSymbolTransferReply.newBuilder();

        SymbolMatchTransfer symbolMatchTransfer = getSourceTransfer(request.getBrokerId(), request.getSymbolId());
        Boolean isOk = false;
        if (null != symbolMatchTransfer) {
            if (symbolMatchTransfer.getSourceExchangeId().equals(symbolMatchTransfer.getMatchExchangeId()) && symbolMatchTransfer.getSourceExchangeId() > 0) {
                 //两个券商同属于一个交易所，没有实际的转发关系
            } else {
                ConfigMatchTransferReply transferReply = bhOrgClient.configMatchTransfer(symbolMatchTransfer.getSourceExchangeId(), symbolMatchTransfer.getMatchExchangeId(), request.getSymbolId(),
                        false, symbolMatchTransfer.getCategory());
                if (transferReply.getCode() != ConfigMatchTransferReply.ReplyCode.SUCCESS) {
                    return SaveSymbolTransferReply.newBuilder().setResult(false).setMessage(getBhMatchTransferError(transferReply.getCode())).build();
                }
            }

            symbolMatchTransfer.setEnable(2); //关闭状态
            isOk = symbolMatchTransferMapper.updateByPrimaryKey(symbolMatchTransfer) > 0 ? true: false;
        }
        return builder.setResult(isOk).build();
    }

    public SaveSymbolTransferReply closeTransferToMe(long brokerId, String symbolId) {
        Example example = new Example(SymbolMatchTransfer.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("matchBrokerId", brokerId);
        criteria.andEqualTo("symbolId", symbolId);
        criteria.andEqualTo("enable", 1);
        List<SymbolMatchTransfer> transfers = symbolMatchTransferMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(transfers)) {
            return SaveSymbolTransferReply.newBuilder().setResult(true).build();
        }
        boolean closeSuc = true;
        for (SymbolMatchTransfer transfer : transfers) {
            if (transfer.getSourceExchangeId() == transfer.getMatchExchangeId() && transfer.getSourceExchangeId() > 0) {
                continue;
            }
            SaveSymbolTransferReply transferReply = closeSymbolTransfer(CloseSymbolTransferRequest.newBuilder()
                    .setBrokerId(transfer.getSourceBrokerId())
                    .setSymbolId(transfer.getSymbolId())
                    .build());
            closeSuc = closeSuc && transferReply.getResult();
            if (!transferReply.getResult()) {
                log.error("close transfer error {}", transfer);
            }
        }
        if (!closeSuc) {
            return SaveSymbolTransferReply.newBuilder().setResult(false).build();
        }
        return SaveSymbolTransferReply.newBuilder().setResult(true).build();
    }

    private String  getBhMatchTransferError(ConfigMatchTransferReply.ReplyCode replyCode) {
        String messageKey = "symbol.match.transfer.failed";
        switch (replyCode) {
            //成功设置
            case SUCCESS:
                messageKey = "request.success";
                break;
            //本交易所对本币对已经是撮合交易所，无法再进行转发
            case SOURCE_SYMBOL_HAS_BEEN_MATCHED:
                messageKey = "symbol.match.transfer.source.is.match";
                break;
            //本交易所的佣金分成小于撮合交易所
            case TARGET_SYMBOL_HAS_GREATER_FEE:
                messageKey = "symbol.match.transfer.target.has.greater.fee";
                break;
            //撮合交易所已经转发了此币对
            case TARGET_SYMBOL_HAS_MATCHED:
                messageKey = "symbol.match.transfer.target.not.match";
                break;
            //目标交易所并不支持本币对
            case TARGET_HAVE_NOT_SYMBOL:
                messageKey = "symbol.match.transfer.target.have.not.symbol";
                break;
            case UPDATE_ERROR:
                messageKey = "symbol.match.transfer.failed";
                break;
            case TARGET_EXCHANGE_NOT_OWNER:
                messageKey = "symbol.match.transfer.target.not.owner";
                break;
        }
        log.warn("Symbol MatchTransfer Exception: {}.", messageKey);
        return messageKey;
    }
}