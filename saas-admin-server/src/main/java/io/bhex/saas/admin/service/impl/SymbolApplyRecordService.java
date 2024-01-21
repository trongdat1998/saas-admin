package io.bhex.saas.admin.service.impl;

import com.google.common.collect.Maps;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.exadmin.SymbolRecord;
import io.bhex.base.token.SymbolDetail;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.dao.ContractApplyMapper;
import io.bhex.saas.admin.dao.SymbolApplyRecordMapper;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.model.ContractApplyRecord;
import io.bhex.saas.admin.model.SymbolApplyRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service
@Slf4j
public class SymbolApplyRecordService {

    @Autowired
    private SymbolApplyRecordMapper applyRecordMapper;

    @Autowired
    private ContractApplyMapper contractApplyMapper;
    @Autowired
    private BhOrgClient bhOrgClient;
    @Autowired
    private BrokerSymbolClient brokerSymbolClient;

    @Transactional(rollbackFor = Throwable.class)
    public int saveSymbolRecord(SymbolApplyRecord symbolApplyRecord) {
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(symbolApplyRecord.getBrokerId());
//        if (exchangeReply == null) {
//            log.error("{} not trusted broker.", symbolApplyRecord.getBrokerId());
//            return -3;
//        }
        Example symbolExample = new Example(SymbolApplyRecord.class);
        symbolExample.createCriteria().andEqualTo("symbolId", symbolApplyRecord.getSymbolId());
        SymbolApplyRecord sr = applyRecordMapper.selectOneByExample(symbolExample);
        if (Objects.nonNull(sr) && symbolApplyRecord.getId() == 0) {
            return -1;
        }
        symbolApplyRecord.setExchangeId(exchangeReply.getExchangeId());

        if (Objects.isNull(sr)) {
            symbolApplyRecord.setId(null);
            return applyRecordMapper.insertSelective(symbolApplyRecord);
        }
        if (sr.getState().equals(ApplyStateEnum.APPLYING.getState())) {
            return -2;
        }
        symbolApplyRecord.setId(sr.getId());
        Example example = new Example(SymbolApplyRecord.class);
        example.setForUpdate(true);
        example.createCriteria().andEqualTo("id", symbolApplyRecord.getId());
        symbolApplyRecord.setState(ApplyStateEnum.APPLYING.getState());
        symbolApplyRecord.setUpdateAt(new Date());
        symbolApplyRecord.setCreateAt(null);
        symbolApplyRecord.setSymbolId(null);
        return applyRecordMapper.updateByPrimaryKeySelective(symbolApplyRecord);
    }

    @Transactional(rollbackFor = Throwable.class)
    public int changeSymbolRecordBrokerId(Long brokerId, String symbolId, Long toBrokerId) {
        Example symbolExample = new Example(SymbolApplyRecord.class);
        symbolExample.createCriteria()
            .andEqualTo("brokerId", brokerId)
            .andEqualTo("symbolId", symbolId);
        SymbolApplyRecord symbolRecord = applyRecordMapper.selectOneByExample(symbolExample);
        if (Objects.isNull(symbolRecord)) {
            return -1;
        }

        Example example = new Example(SymbolApplyRecord.class);
        example.createCriteria()
            .andEqualTo("brokerId", symbolRecord.getBrokerId())
            .andEqualTo("symbolId", symbolRecord.getSymbolId());

        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(toBrokerId);
        brokerSymbolClient.changeSymbolApplyBroker(symbolId, symbolRecord.getExchangeId(), exchangeReply.getExchangeId(), brokerId, toBrokerId);

        symbolRecord.setBrokerId(toBrokerId);
        symbolRecord.setExchangeId(exchangeReply.getExchangeId());
        return applyRecordMapper.updateByExampleSelective(symbolRecord, example);
    }


    public int applyContract(ContractApplyRecord contractApplyRecord) {
        SymbolDetail symbolDetail = brokerSymbolClient.getBhSymbol(contractApplyRecord.getSymbolId());
        if (Objects.nonNull(symbolDetail) && StringUtils.isNotEmpty(symbolDetail.getSymbolId())) {
            throw new BizException(ErrorCode.SYMBOL_ALREADY_EXIST);
        }

        Example symbolExample = new Example(ContractApplyRecord.class);
        symbolExample.createCriteria().andEqualTo("symbolId", contractApplyRecord.getSymbolId());
        ContractApplyRecord sr = contractApplyMapper.selectOneByExample(symbolExample);
        if (Objects.nonNull(sr) && contractApplyRecord.getId() == 0) {
            return -1;
        }

        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(contractApplyRecord.getBrokerId());
        contractApplyRecord.setExchangeId(exchangeReply.getExchangeId());
        if (Objects.isNull(sr)) {
            contractApplyRecord.setId(null);
            contractApplyRecord.setCreatedAt(System.currentTimeMillis());
            return contractApplyMapper.insertSelective(contractApplyRecord);
        }
        if (sr.getState().equals(ApplyStateEnum.APPLYING.getState())) {
            return -2;
        }
        contractApplyRecord.setId(sr.getId());
        Example example = new Example(ContractApplyRecord.class);
        example.setForUpdate(true);
        example.createCriteria().andEqualTo("id", contractApplyRecord.getId());
        contractApplyRecord.setState(ApplyStateEnum.APPLYING.getState());
        contractApplyRecord.setUpdatedAt(System.currentTimeMillis());
        contractApplyRecord.setCreatedAt(null);
        contractApplyRecord.setSymbolId(null);
        return contractApplyMapper.updateByPrimaryKeySelective(contractApplyRecord);
    }

    public Map<String, Long> getSymbolApplyBrokerMap(List<String> symbols) {
        if (CollectionUtils.isEmpty(symbols)) {
            return Maps.newHashMap();
        }
        Example example = new Example(SymbolApplyRecord.class);
        example.createCriteria().andIn("symbolId", symbols);
        List<SymbolApplyRecord> records = applyRecordMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(records)) {
            return Maps.newHashMap();
        }
        Map<String, Long> result = Maps.newHashMap();
        records.forEach(r -> result.put(r.getSymbolId(), r.getBrokerId()));
        return result;
    }

    public Map<String, Long> getContractApplyBrokerMap(List<String> symbols) {
        if (CollectionUtils.isEmpty(symbols)) {
            return Maps.newHashMap();
        }
        Example example = new Example(ContractApplyRecord.class);
        example.createCriteria().andIn("symbolId", symbols);
        List<ContractApplyRecord> records = contractApplyMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(records)) {
            return Maps.newHashMap();
        }
        Map<String, Long> result = Maps.newHashMap();
        records.forEach(r -> result.put(r.getSymbolId(), r.getBrokerId()));
        return result;
    }

//    public void auditSymbolFuturesRecord(AuditFuturesRequest request, StreamObserver<SymbolFuturesRecordResult> responseObserver) {
//        io.bhex.ex.admingrpc.model.SymbolFuturesRecord symbolRecord = mapper.selectByPrimaryKey(request.getId());
//        if (Objects.nonNull(symbolRecord)) {
//            if (symbolRecord.getState().equals(request.getCurState())) {
//                symbolRecord.setState(request.getUpdatedState());
//                symbolRecord.setIndexToken(request.getIndexToken());
//                symbolRecord.setDisplayIndexToken(request.getDisplayIndexToken());
//                // saas admin 指定symbol id
//                String newSymbolId = request.getSetSymbolId();
//                if (StringUtils.isNotEmpty(newSymbolId)) {
//                    io.bhex.ex.admingrpc.model.SymbolFuturesRecord byNewSymbolId = mapper.getBySymbolId(newSymbolId);
//                    if (Objects.nonNull(byNewSymbolId) && !byNewSymbolId.getId().equals(symbolRecord.getId())) {
//                        responseObserver.onNext(SymbolFuturesRecordResult.newBuilder()
//                                .setRes(50014)
//                                .build());
//                        responseObserver.onCompleted();
//                    }
//                    symbolRecord.setSymbolId(newSymbolId);
//                    symbolRecord.setSymbolName(newSymbolId);
//                    symbolRecord.setBaseTokenId(newSymbolId);
//                }
//
//                Example example = new Example(io.bhex.ex.admingrpc.model.SymbolFuturesRecord.class);
//                example.createCriteria()
//                        .andEqualTo("id", symbolRecord.getId())
//                        .andEqualTo("state", request.getCurState());
//                mapper.updateByExampleSelective(symbolRecord, example);
//                responseObserver.onNext(SymbolFuturesRecordResult.newBuilder()
//                        .setRes(ResultEnum.SUCCESS.getCode())
//                        .setSymbolRecord(symbolRecord.toProtoObj())
//                        .build());
//                responseObserver.onCompleted();
//            }
//        } else {
//            responseObserver.onNext(SymbolFuturesRecordResult.newBuilder()
//                    .setRes(-1)
//                    .build());
//            responseObserver.onCompleted();
//        }
//    }



}
