package io.bhex.saas.admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.bhex.base.bhadmin.*;
import io.bhex.base.token.PublishFuturesReply;
import io.bhex.base.token.SymbolDetail;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.controller.dto.SymbolFuturesRecordDTO;
import io.bhex.saas.admin.controller.param.AuditFuturesPO;
import io.bhex.saas.admin.dao.ContractApplyMapper;
import io.bhex.saas.admin.enums.EventTypeEnum;
import io.bhex.saas.admin.exception.PublishFuturesException;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.grpc.client.impl.ExchangeSwapClientImpl;
import io.bhex.saas.admin.model.ContractApplyRecord;
import io.bhex.saas.admin.model.EventLog;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.ExchangeSwapService;
import io.bhex.saas.admin.service.BrokerSymbolService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 2019/10/10 7:52 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class ExchangeSwapServiceImpl implements ExchangeSwapService {

    @Autowired
    private ContractApplyMapper contractApplyMapper;

    @Autowired
    private ExchangeSwapClientImpl exchangeSwapClient;

    @Autowired
    private EventLogService eventLogService;

    @Autowired
    private BrokerSymbolClient brokerSymbolClient;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private BrokerSymbolService brokerSymbolService;

    @Override
    public PaginationVO<SymbolFuturesRecordDTO> applicationList(Integer current, Integer pageSize, Integer state) {
        GetSymbolPager pager = GetSymbolPager.newBuilder()
                .setStart(current)
                .setSize(pageSize)
                .setState(state)
                .setBrokerId(-1)
                .build();
        ContractApplyList recordList = listContractApply(pager);
        PaginationVO<SymbolFuturesRecordDTO> symbolRecordDTOPaginationVO = new PaginationVO<>();
        symbolRecordDTOPaginationVO.setList(recordList.getRecordList().stream()
                .map(SymbolFuturesRecordDTO::parseFromProtoObj)
                .collect(Collectors.toList()));

        Map<Long, String> brokerNameMap = brokerService.queryBrokerName();
        for (SymbolFuturesRecordDTO symbolFuturesRecordDTO : symbolRecordDTOPaginationVO.getList()) {
            symbolFuturesRecordDTO.setBrokerName(brokerNameMap.getOrDefault(symbolFuturesRecordDTO.getBrokerId(), ""));
            EventLog eventLog = eventLogService.getUnSuccessEvent(symbolFuturesRecordDTO.getBrokerId(), symbolFuturesRecordDTO.getId());
            if (eventLog != null) {
                symbolFuturesRecordDTO.setUpdateStatus(eventLog.getStatus());
            }
        }

        symbolRecordDTOPaginationVO.setCurrent(current);
        symbolRecordDTOPaginationVO.setPageSize(Math.min(recordList.getTotal(), pageSize));
        symbolRecordDTOPaginationVO.setTotal(recordList.getTotal());
        return symbolRecordDTOPaginationVO;
    }



    @Override
    public int auditSymbolRecord(AuditFuturesPO auditPO) {
        if (StringUtils.isNotEmpty(auditPO.getNewSymbolId())) {
            SymbolDetail symbol = brokerSymbolClient.getBhSymbol(auditPO.getNewSymbolId());
            if (Objects.nonNull(symbol)) {
                throw new BizException(ErrorCode.SYMBOL_ID_ALREADY_EXIST);
            }
        }

        ContractApplyRecord applyRecord = contractApplyMapper.selectByPrimaryKey(auditPO.getId());
        if (applyRecord.getState().equals(auditPO.getCurState())) {
            applyRecord.setState(auditPO.getToState());
            applyRecord.setIndexToken(auditPO.getIndexToken());
            applyRecord.setDisplayIndexToken(auditPO.getDisplayIndexToken());
            // saas admin 指定symbol id
            String newSymbolId = auditPO.getNewSymbolId();
            if (StringUtils.isNotEmpty(newSymbolId)) {
                ContractApplyRecord byNewSymbolId = contractApplyMapper.getBySymbolId(newSymbolId);
                if (Objects.nonNull(byNewSymbolId) && !byNewSymbolId.getId().equals(applyRecord.getId())) {
                    throw new BizException(ErrorCode.SYMBOL_ALREADY_EXIST);
                }
                applyRecord.setSymbolId(newSymbolId);
                applyRecord.setSymbolName(newSymbolId);
                applyRecord.setBaseTokenId(newSymbolId);
            }

            Example example = new Example(ContractApplyRecord.class);
            example.createCriteria()
                    .andEqualTo("id", applyRecord.getId())
                    .andEqualTo("state", auditPO.getCurState());
            contractApplyMapper.updateByExampleSelective(applyRecord, example);

        }




        EventLog eventLog = eventLogService.addEvent(applyRecord.getBrokerId(),
                applyRecord.getId(), applyRecord.getSymbolId(), EventTypeEnum.AUDIT_CONTRACT_SUCCESS);
        eventLogService.doEvent(eventLog);
        return 0;
    }

    public void auditSymbolFull(long applyId, long eventId) {
        ContractApplyRecord applyRecord = contractApplyMapper.selectByPrimaryKey(applyId);
        if (applyRecord.getState() != ApplyStateEnum.ACCEPT.getState()) {
            return;
        }

        PublishFuturesReply reply = exchangeSwapClient.publishFutures(applyRecord);
        if (!reply.getResult().equals(PublishFuturesReply.PublishFuturesErrorCode.SUCCESS)) {
            throw new PublishFuturesException(String.valueOf(reply.getResult().getNumber()));
        }

        brokerSymbolService.publishBrokerSymbol(applyRecord.getBrokerId(), applyRecord.getSymbolId(), eventId);

    }

    public ContractApplyList listContractApply(GetSymbolPager request) {
        Example example = new Example(ContractApplyRecord.class);
        Example.Criteria criteria = example.createCriteria();
        if (request.getBrokerId() > 0) {
            criteria.andEqualTo("brokerId", request.getBrokerId());
        }
        if (request.getState() != -1) {
            criteria.andEqualTo("state", request.getState());
        }
        example.orderBy("updatedAt").desc();
        Page page = PageHelper.startPage(request.getStart(), request.getSize());
        List<ContractApplyObj> resultList = new ArrayList<>();
        List<ContractApplyRecord> symbolRecordList = contractApplyMapper.selectByExample(example);
        if (Objects.nonNull(symbolRecordList)) {
            resultList = symbolRecordList
                    .stream()
                    .map(r -> toProtoObj(r))
                    .collect(Collectors.toList());
        }
        return ContractApplyList.newBuilder()
                .addAllRecord(resultList)
                .setTotal((int) page.getTotal())
                .build();
    }

    private ContractApplyObj toProtoObj(ContractApplyRecord record) {
        ContractApplyObj applyObj = record.toProtoObj();
        EventLog eventLog = eventLogService.getUnSuccessEvent(record.getBrokerId(), record.getId());
        if (eventLog != null) {
            applyObj = applyObj.toBuilder().setUpdateStatus(eventLog.getStatus()).build();
        }
        return applyObj;
    }

}
