package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.exadmin.GetSymbolFuturesPager;
import io.bhex.base.exadmin.SymbolFuturesRecordList;
import io.bhex.base.exadmin.SymbolFuturesRecordServiceGrpc;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.PublishFuturesReply;
import io.bhex.base.token.PublishFuturesRequest;
import io.bhex.base.token.SymbolServiceGrpc;
import io.bhex.saas.admin.grpc.client.ExchangeSwapClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.model.ContractApplyRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.client.impl
 * @Author: ming.xu
 * @CreateDate: 2019/10/10 5:18 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class ExchangeSwapClientImpl implements ExchangeSwapClient {

    @Resource
    private GrpcClientConfig grpcConfig;

    private SymbolServiceGrpc.SymbolServiceBlockingStub getStub() {
        return grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private SymbolFuturesRecordServiceGrpc.SymbolFuturesRecordServiceBlockingStub getCommonStub() {
        return grpcConfig.symbolFuturesRecordServiceBlockingStub();
    }


    @Override
    public SymbolFuturesRecordList swapApplicationList(Integer current, Integer pageSize, Integer state) {
        GetSymbolFuturesPager pager = GetSymbolFuturesPager.newBuilder()
                .setStart(current)
                .setSize(pageSize)
                .setState(state)
                .setExchangeId(-1)
                .build();
        return getCommonStub().listSymbolFuturesRecord(pager);
    }

//    @Override
//    public int auditSwapRecord(AuditFuturesPO auditPO) {
//        AuditFuturesRequest request = AuditFuturesRequest.newBuilder()
//                .setCurState(auditPO.getCurState())
//                .setUpdatedState(auditPO.getToState())
//                .setId(auditPO.getId())
//                .setIndexToken(auditPO.getIndexToken())
//                .setDisplayIndexToken(auditPO.getDisplayIndexToken())
//                .setReason(auditPO.getSymbolId())
//                .setSetSymbolId(Strings.nullToEmpty(auditPO.getNewSymbolId()))
//                .build();
//        SymbolFuturesRecordResult result = getCommonStub().auditSymbolFuturesRecord(request);
//
//        if (Objects.nonNull(result) && result.getRes() == ErrorCode.SYMBOL_ID_ALREADY_EXIST.getCode()) {
//            throw new BizException(ErrorCode.SYMBOL_ID_ALREADY_EXIST);
//        }
//        SymbolFuturesRecord symbolRecord = result.getSymbolRecord();
//        if (symbolRecord.getState() == ApplyStateEnum.ACCEPT.getState()) {
//            PublishFuturesReply reply = null;
//            try {
//                reply = getStub().publishFutures(toPublishFuturesProto(symbolRecord));
//                if (!reply.getResult().equals(PublishFuturesReply.PublishFuturesErrorCode.SUCCESS)) {
//                    throw new PublishFuturesException(String.valueOf(reply.getResult().getNumber()));
//                }
//            } catch (Exception e) {
//                log.error(e.getMessage(), e);
//                int curS = request.getCurState();
//                int updateS = request.getUpdatedState();
//                request = request.toBuilder()
//                        .setUpdatedState(curS)
//                        .setCurState(updateS)
//                        .build();
//                getCommonStub().auditSymbolFuturesRecord(request);
//                log.info("Will revert symbol record state from  [{}] to [{}]",
//                        request.getCurState(), request.getUpdatedState());
//                if (Objects.nonNull(reply) && !reply.getResult().equals(PublishFuturesReply.PublishFuturesErrorCode.SUCCESS)) {
//                    throw new PublishFuturesException(String.valueOf(reply.getResult().getNumber()));
//                }
//                throw new BizException(ErrorCode.TRY_AGAIN_LATER);
//            }
//        }
//        return result.getRes();
//    }

    @Override
    public PublishFuturesReply publishFutures(ContractApplyRecord applyRecord) {
        return getStub().publishFutures(toPublishFuturesProto(applyRecord));
    }

    private PublishFuturesRequest toPublishFuturesProto(ContractApplyRecord applyRecord) {
        return PublishFuturesRequest.newBuilder()
                .setSymbolId(applyRecord.getSymbolId())
                .setSymbolName(applyRecord.getSymbolName())
                .setBaseTokenId(applyRecord.getBaseTokenId())
                .setQuoteTokenId(applyRecord.getQuoteTokenId())
                .setUnderlyingId(applyRecord.getUnderlyingId())
                .setDisplayUnderlyingId(applyRecord.getDisplayUnderlyingId())
                .setExchangeId(applyRecord.getExchangeId())
                .setBrokerId(applyRecord.getBrokerId())
                .setMinTradeQuantity(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeQuantity()))
                .setMinTradeAmount(DecimalUtil.fromBigDecimal(applyRecord.getMinTradeAmount()))
                .setMinPricePrecision(DecimalUtil.fromBigDecimal(applyRecord.getMinPricePrecision()))
                .setDigitMergeList(applyRecord.getDigitMergeList())
                .setBasePrecision(DecimalUtil.fromBigDecimal(applyRecord.getBasePrecision()))
                .setQuotePrecision(DecimalUtil.fromBigDecimal(applyRecord.getQuotePrecision()))
                .setDisplayToken(applyRecord.getDisplayToken())
                .setCurrency(applyRecord.getCurrency())
                .setCurrencyDisplay(applyRecord.getCurrencyDisplay())
                .setContractMultiplier(DecimalUtil.fromBigDecimal(applyRecord.getContractMultiplier()))
                .setLimitDownInTradingHours(DecimalUtil.fromBigDecimal(applyRecord.getLimitDownInTradingHours()))
                .setLimitUpInTradingHours(DecimalUtil.fromBigDecimal(applyRecord.getLimitUpInTradingHours()))
                .setLimitDownOutTradingHours(DecimalUtil.fromBigDecimal(applyRecord.getLimitDownOutTradingHours()))
                .setLimitUpOutTradingHours(DecimalUtil.fromBigDecimal(applyRecord.getLimitUpOutTradingHours()))
                .setMaxLeverage(DecimalUtil.fromBigDecimal(applyRecord.getMaxLeverage()))
                .setLeverageRange(applyRecord.getLeverageRange())
                .setOverPriceRange(applyRecord.getOverPriceRange())
                .setMarketPriceRange(applyRecord.getMarketPriceRange())
                .setIsPerpetualSwap(applyRecord.getIsPerpetualSwap())
                .setIndexToken(applyRecord.getIndexToken())
                .setDisplayIndexToken(applyRecord.getDisplayIndexToken())
                .setFundingLowerBound(DecimalUtil.fromBigDecimal(applyRecord.getFundingLowerBound()))
                .setFundingUpperBound(DecimalUtil.fromBigDecimal(applyRecord.getFundingUpperBound()))
                .setFundingInterest(DecimalUtil.fromBigDecimal(applyRecord.getFundingInterest()))
                .setIsReverse(applyRecord.getIsReverse())
                .setMarginPrecision(DecimalUtil.fromBigDecimal(applyRecord.getMarginPrecision()))
                .setSymbolNameLocaleJson(applyRecord.getSymbolNameLocaleJson())
                .setRiskLimitJson(applyRecord.getRiskLimitJson())
                .build();
    }
}
