package io.bhex.saas.admin.grpc.server;

import com.google.protobuf.TextFormat;
import io.bhex.base.bhadmin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.saas.admin.dao.SymbolApplyRecordMapper;
import io.bhex.saas.admin.grpc.client.BrokerSymbolClient;
import io.bhex.saas.admin.model.ContractApplyRecord;
import io.bhex.saas.admin.model.SymbolApplyRecord;
import io.bhex.saas.admin.service.BrokerSymbolService;
import io.bhex.saas.admin.service.ExchangeSwapService;
import io.bhex.saas.admin.service.impl.EventLogService;
import io.bhex.saas.admin.service.impl.SymbolApplyRecordService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.Objects;

@GrpcService
@Slf4j
public class AdminSymbolApplyGrpcService extends AdminSymbolApplyServiceGrpc.AdminSymbolApplyServiceImplBase {
    @Autowired
    private SymbolApplyRecordMapper applyRecordMapper;
    @Autowired
    private SymbolApplyRecordService applyRecordService;

    @Autowired
    private BrokerSymbolService brokerSymbolService;

    @Autowired
    ExchangeSwapService exchangeSwapService;
    @Autowired
    private EventLogService eventLogService;
    @Autowired
    private BrokerSymbolClient brokerSymbolClient;

    @Override
    public void closeSymbol(CloseSymbolRequest request, StreamObserver<CloseSymbolResult> responseObserver) {
//        SymbolMatchTransfer sourceTransfer = symbolTransferService.getSourceTransfer(request.getBrokerId(), request.getSymbolId());
//        if (sourceTransfer != null && sourceTransfer.getEnable() == 1) {
//            responseObserver.onNext(CloseSymbolResult.newBuilder().setRes(1).setMessage("symbol.transfer.not.close").build());
//            responseObserver.onCompleted();
//            return;
//        }

        io.bhex.broker.grpc.admin.SymbolDetail symbolDetail = brokerSymbolClient.queryBrokerSymbolById(request.getBrokerId(), request.getSymbolId());
        if (!symbolDetail.getPublished()) {
            responseObserver.onNext(CloseSymbolResult.newBuilder().setRes(0).build());
            responseObserver.onCompleted();
            return;
        }

        if (eventLogService.hasUndoneTask(request.getBrokerId(), request.getSymbolId())) {
            log.info("closeSymbol:{} not end", TextFormat.shortDebugString(request));
            responseObserver.onNext(CloseSymbolResult.newBuilder().setRes(1).setMessage("please.wait").build());
            responseObserver.onCompleted();
            return;
        }
//        EventLog eventLog = eventLogService.addEvent(request.getBrokerId(), 0, request.getSymbolId(), EventTypeEnum.CLOSE_BROKER_SYMBOL);
//        eventLogService.doEvent(eventLog);
        CloseSymbolResult result = brokerSymbolService.closeSymbol(request.getBrokerId(), request.getSymbolId(), 0L);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void openSymbol(OpenSymbolRequest request, StreamObserver<OpenSymbolResult> responseObserver) {
        if (eventLogService.hasUndoneTask(request.getBrokerId(), request.getSymbolId())) {
            log.info("openSymbol:{} not end", TextFormat.shortDebugString(request));
            responseObserver.onNext(OpenSymbolResult.newBuilder().setRes(1).setMessage("please.wait").build());
            responseObserver.onCompleted();
            return;
        }
        OpenSymbolResult result = brokerSymbolService.publishBrokerSymbol(request.getBrokerId(), request.getSymbolId(), 0L);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void queryUpdatingSymbols(QueryUpdatingSymbolsRequest request, StreamObserver<QueryUpdatingSymbolsResult> responseObserver) {
        QueryUpdatingSymbolsResult result = eventLogService.getUpdatingSymbols(request.getBrokerId(), request.getSymbolIdList());
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void listApplyRecords(GetSymbolPager request, StreamObserver<SymbolApplyRecordList> responseObserver) {
        SymbolApplyRecordList result = brokerSymbolService.listApplyRecords(request);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void applySymbol(SymbolApplyObj request, StreamObserver<ApplySymbolResult> responseObserver) {
        SymbolApplyRecord symbolRecord =
                SymbolApplyRecord.parseFromProtoObj(request);
        int res = applyRecordService.saveSymbolRecord(symbolRecord);
        responseObserver.onNext(ApplySymbolResult.newBuilder()
                .setRes(res)
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void auditApplySymbol(AuditSymbolApplyRequest request, StreamObserver<ApplySymbolResult> responseObserver) {
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
        responseObserver.onNext(ApplySymbolResult.newBuilder()
                .setRes(0)
                .setSymbolRecord(symbolRecord.toProtoObj())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void querySymbolRecord(SymbolApplyObj request, StreamObserver<SymbolApplyObj> responseObserver) {
        Example example = new Example(SymbolApplyRecord.class);
        example.createCriteria()
                .andEqualTo("brokerId", request.getBrokerId())
                .andEqualTo("symbolId", request.getSymbolId());
        SymbolApplyRecord symbolRecord = applyRecordMapper.selectOneByExample(example);
        if (Objects.isNull(symbolRecord)) {
            responseObserver.onNext(SymbolApplyObj.getDefaultInstance());
        } else {
            responseObserver.onNext(symbolRecord.toProtoObj());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void changeSymbolBrokerId(ChangeSymbolBrokerIdRequest request, StreamObserver<ChangeSymbolBrokerIdIdResult> responseObserver) {
        int res = applyRecordService.changeSymbolRecordBrokerId(request.getBrokerId(),
                request.getSymbolId(), request.getToBrokerId());
        responseObserver.onNext(ChangeSymbolBrokerIdIdResult.newBuilder()
                .setRes(res)
                .build());
        responseObserver.onCompleted();
    }


    @Override
    public void listContractApplyRecord(GetSymbolPager request, StreamObserver<ContractApplyList> responseObserver) {
        ContractApplyList result = exchangeSwapService.listContractApply(request);
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

    @Override
    public void applyContract(ContractApplyObj request, StreamObserver<ContractApplyResult> responseObserver) {
        ContractApplyRecord applyRecord = ContractApplyRecord.parseFromProtoObj(request);
        int res = applyRecordService.applyContract(applyRecord);
        responseObserver.onNext(ContractApplyResult.newBuilder().setRes(res).build());
        responseObserver.onCompleted();
    }


    @Override
    public void auditApplyContract(AuditContractRequest request, StreamObserver<ContractApplyResult> responseObserver) {

    }
}
