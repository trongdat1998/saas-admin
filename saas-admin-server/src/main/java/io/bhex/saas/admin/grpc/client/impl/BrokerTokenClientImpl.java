package io.bhex.saas.admin.grpc.client.impl;

import com.google.common.base.Strings;
import io.bhex.base.bhadmin.*;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.quote.FairValue;
import io.bhex.base.quote.GetFairValueRequest;
import io.bhex.base.quote.QuoteServiceGrpc;
import io.bhex.base.quote.UpdateRateRequest;
import io.bhex.base.token.QueryTokenRequest;
import io.bhex.base.token.TokenDetail;
import io.bhex.base.token.*;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.broker.grpc.admin.*;
import io.bhex.broker.grpc.common.AdminSimplyReply;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.controller.param.TokenAuditPO;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.model.TokenApplyRecord;
import io.bhex.saas.admin.service.impl.AdminTokenApplyService;
import io.grpc.Deadline;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static io.bhex.saas.admin.controller.dto.TokenRecordDTO.getFeeTokenByValue;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@Service
public class BrokerTokenClientImpl implements BrokerTokenClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;
    @Autowired
    private AdminTokenApplyService adminTokenApplyService;


    public TokenServiceGrpc.TokenServiceBlockingStub getStub() {
        return grpcConfig.tokenServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    public QuoteServiceGrpc.QuoteServiceBlockingStub getQuoteStub() {
        return grpcConfig.quoteServiceBlockingStub(GrpcClientConfig.QUOTE_CHANNEL_NAME);
    }

    public AdminTokenServiceGrpc.AdminTokenServiceBlockingStub getBrokerStub(long brokerId) {
        AdminTokenServiceGrpc.AdminTokenServiceBlockingStub stub = AdminTokenServiceGrpc.newBlockingStub(brokerServerChannelRouter.getChannelByBrokerId(brokerId))
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
        return stub;
    }


    @Override
    public TokenDetailInfo getTokenDetailInfo(String tokenId) {
        return getStub().getTokenInfo(GetTokenRequest.newBuilder()
            .setTokenId(tokenId)
            .build());
    }

    @Override
    public FairValue getFairValue(Long exchangeId, String tokenId) {
        return getQuoteStub().getFairValue(GetFairValueRequest.newBuilder()
            .setExchangeId(exchangeId)
            .setTokenId(tokenId)
            .build());
    }

    @Override
    public int setFairValue(Long exchangeId, String tokenId, BigDecimal usd) {
        return getQuoteStub().updateRate(UpdateRateRequest.newBuilder()
            .setExchangeId(exchangeId)
            .setToken(tokenId)
            .setUsdt(usd.doubleValue())
            .build()).getCode();
    }

    @Override
    public TokenApplyObj getTokenRecord(Long brokerId, String tokenId) {
        return TokenApplyRecord.toProtoObj(adminTokenApplyService.queryTokenRecord(QueryApplyTokenRecordRequest.newBuilder()
            .setBrokerId(brokerId)
            .setTokenId(tokenId)
            .build()));
    }

    @Override
    public TokenApplyObj getTokenRecordById(Long brokerId, Long tokenRecordId) {
        return TokenApplyRecord.toProtoObj(adminTokenApplyService.queryTokenRecord(QueryApplyTokenRecordRequest.newBuilder()
                .setBrokerId(brokerId)
                .setId(tokenRecordId)
                .build()));
    }

    @Override
    public GetTokensReply getBhTokensNoCache(Integer current, Integer pageSize, Integer category, Integer tokenType, String tokenId) {
        GetTokensRequest.Builder builder = GetTokensRequest.newBuilder()
            .setCurrent(current)
            .setPageSize(pageSize);
        if (Objects.nonNull(category) && !category.equals(0L)) {
            builder.setCategory(category);
        }
        if (Objects.nonNull(tokenType) && !tokenType.equals(0L)) {
            builder.setTokenType(tokenType);
        }
        if (StringUtils.isNotEmpty(tokenId)) {
            builder.setTokenId(tokenId);
        }
        return getStub().getTokens(builder.build());
    }

    @Override
    public QueryTokensReply queryBhTokens(Integer current, Integer pageSize, Integer category, Integer tokenType, String tokenId) {
        QueryTokenRequest.Builder builder = QueryTokenRequest.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize);
        if (Objects.nonNull(category) && !category.equals(0L)) {
            builder.setCategory(category);
        }

        if (StringUtils.isNotEmpty(tokenId)) {
            builder.setTokenId(tokenId);
        }
        return getStub().queryTokens(builder.build());
    }

    @Override
    public boolean switchDepositWithdraw(String tokenId, Boolean allowDeposit, Boolean allowWithdraw, Boolean addressNeedTag) {
        UpdateTokenSettingRequest request = UpdateTokenSettingRequest.newBuilder()
                .setTokenId(tokenId)
                .setAllowDeposit(allowDeposit)
                .setAllowWithdraw(allowWithdraw)
                .setAddressNeedTag(addressNeedTag)
                .build();
        try {
            getStub().updateTokenSetting(request);
            return true;
        } catch (Exception e) {
            log.error("switchDepositWithdraw error. ", e);
            return false;
        }
    }

    @Override
    public TokenDetail getBhToken(String tokenId) {
        GetTokenRequest.Builder builder = GetTokenRequest.newBuilder()
            .setTokenId(tokenId);

        return getStub().getToken(builder.build());
    }

    @Override
    public TokenApplyRecordList applicationList(Long brokerId, Integer current, Integer pageSize, int state, String token, String contractAddress) {
        return adminTokenApplyService.listTokenApplyRecords(GetTokenPager.newBuilder()
            .setBrokerId(brokerId == null || brokerId <= 0 ? -1 : brokerId)
            .setToken(Strings.nullToEmpty(token))
            .setContractAddress(Strings.nullToEmpty(contractAddress))
            .setTokenType(-1)
            .setStart(current)
            .setSize(pageSize)
            .setState(state)
            .build());
    }

    @Override
    public int auditTokenRecord(TokenAuditPO auditPO, String introduction, long applyBrokerId, long applyExchangeId) {
        String feeToken = getFeeTokenByValue(auditPO.getTokenId(), auditPO.getFeeToken(), auditPO.getParentTokenId());
        boolean isPrivateToken = auditPO.getIsBaas() || auditPO.getIsPrivateToken(); //Baas一定是私有。私有不一定是Baas
        AuditTokenApplyRequest request = AuditTokenApplyRequest.newBuilder()
            .setCurState(auditPO.getCurState())
            .setUpdatedState(auditPO.getToState())
            .setReason(Strings.nullToEmpty(auditPO.getReason()))
            .setId(auditPO.getId())
            .setFeeToken(feeToken)
            .setPlatformFee(DecimalUtil.fromBigDecimal(auditPO.getPlatformFee()))
            .setExploreUrl(Strings.nullToEmpty(auditPO.getExploreUrl()))
            .setConfirmCount(auditPO.getConfirmCount())
            .setCanWithdrawConfirmCount(Optional.ofNullable(auditPO.getCanWithdrawConfirmCount()).orElse(0))
            .setMinPrecision(auditPO.getMinPrecision())
            .setNeedTag(auditPO.isNeedTag())
            .setTokenId(auditPO.getTokenId())
            .setTokenName(auditPO.getTokenName())
            .setTokenFullName(auditPO.getTokenFullName())
            .setFairValue(DecimalUtil.fromBigDecimal(auditPO.getFairValue()))
            .setIcoUrl(auditPO.getIconUrl())
            .setTokenType(auditPO.getTokenType())
            .setContractAddress(auditPO.getContractAddress())
            .setIntroduction(introduction)
            .setMinDepositingAmt(DecimalUtil.fromBigDecimal(auditPO.getMinDepositingAmt()))
            .setMinWithdrawingAmt(DecimalUtil.fromBigDecimal(auditPO.getMinWithdrawingAmt()))
            .setMaxQuantitySupplied(Strings.nullToEmpty(auditPO.getMaxQuantitySupplied()))
            .setCurrentTurnover(Strings.nullToEmpty(auditPO.getCurrentTurnover()))
            .setOfficialWebsiteUrl(auditPO.getOfficialWebsiteUrl())
            .setWhitePaperUrl(auditPO.getWhitePaperUrl())
            .setPublishTime(auditPO.getPublishTime())
            .setIsPrivateToken(isPrivateToken)
            .setIsAggregate(auditPO.getIsAggregate())
            .setIsBaas(auditPO.getIsBaas())
            .setIsTest(auditPO.getIsTest())
                .setChainName(auditPO.getChainName())
                .setParentTokenId(auditPO.getParentTokenId())
                .setChainSequence(auditPO.getChainSequence())
            .build();
        TokenApplyObj tokenRecord = adminTokenApplyService.auditApplyToken(request);
        int tokenIndex = 0;
        log.info("exgrpc tokenRecord:{}", tokenRecord);
        if (tokenRecord.getState() == ApplyStateEnum.ACCEPT.getState()) {
            try {
                setFairValue(tokenRecord.getExchangeId(), tokenRecord.getTokenId(), DecimalUtil.toBigDecimal(tokenRecord.getFairValue()));
                PublishTokenReply reply = getStub().publishToken(PublishTokenRequest.newBuilder()
                        .setTokenIndex(tokenIndex)
                    .setConfirmAmt(tokenRecord.getConfirmCount())
                    .setCanWithdrawConfirmCount(tokenRecord.getCanWithdrawConfirmCount())
                    .setDepositMinQuantity(tokenRecord.getMinDepositingAmt())
                    .setExploreUrl(tokenRecord.getExploreUrl())
                    .setFeeToken(tokenRecord.getFeeToken())
                    .setIsNeedTag(tokenRecord.getNeedTag() == 1)
                    .setMinPrecision(tokenRecord.getMinPrecision())
                    .setWithdrawMinQuantity(tokenRecord.getMinWithdrawingAmt())
                    .setTokenName(tokenRecord.getTokenName())
                        .setTokenFullName(tokenRecord.getTokenFullName())
                    .setTokenId(tokenRecord.getTokenId())
                    .setSafeQuantity(DecimalUtil.fromLong(99999999L))
                    .setPlatformFee(tokenRecord.getPlatformFee())
                    .setIcoUrl(tokenRecord.getIconUrl())
                    .setTokenTypeValue(tokenRecord.getTokenType())
                    .setDescription(tokenRecord.getIntroduction())
                    .setMaxQuantitySupplied(Strings.nullToEmpty(auditPO.getMaxQuantitySupplied()))
                    .setCurrentTurnover(Strings.nullToEmpty(auditPO.getCurrentTurnover()))
                    .setOfficialWebsiteUrl(auditPO.getOfficialWebsiteUrl())
                    .setWhitePaperUrl(auditPO.getWhitePaperUrl())
                    .setPublishTime(auditPO.getPublishTime())
                    .setPrivateTokenExchangeId(isPrivateToken ? applyExchangeId : 0)
                    .setPrivateTokenBrokerId(isPrivateToken ? applyBrokerId : 0)
                    .setIsAggregate(auditPO.getIsAggregate())
                    .setIsBaas(auditPO.getIsBaas())
                    .setIsTest(auditPO.getIsTest())
                    .setApplyBrokerId(applyBrokerId)
                        .setChainName(auditPO.getChainName())
                        .setParentTokenId(auditPO.getParentTokenId())
                        .setChainSequence(auditPO.getChainSequence())
                    .build());
                log.info("bh publishToken:{}", reply);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new BizException(ErrorCode.TRY_AGAIN_LATER);
            }

            if (Strings.isNullOrEmpty(auditPO.getParentTokenId())) { //如果是子链币就不要开给券商了
                AddTokenRequest token = AddTokenRequest.newBuilder()
                        .setTokenIndex(tokenIndex)
                        .setTokenId(auditPO.getTokenId())
                        .setTokenName(auditPO.getTokenName())
                        .setTokenFullName(auditPO.getTokenFullName())
                        .setMinPrecision(auditPO.getMinPrecision())
                        .setAddressType("")
                        .setDepositMinQuantity(auditPO.getMinDepositingAmt().stripTrailingZeros().toPlainString())
                        .setAllowDeposit(false)
                        .setAllowWithdraw(false)
                        .setIcon(auditPO.getIconUrl())
                        .setTokenDetail("")
                        .setBrokerId(applyBrokerId)
                        .setCategory(TokenCategory.MAIN_CATEGORY_VALUE)
                        .build();
                getBrokerStub(applyBrokerId).addToken(token);
            }
        }



        return 1;
    }

    @Override
    public AddTokenReply initBrokerToken(long brokerId, String tokenId, int category) {
        GetTokensReply reply = getBhTokensNoCache(1, 1, category, null, tokenId);
        TokenDetail tokenDetail = reply.getTokenDetails(0);
        AddTokenRequest token = AddTokenRequest.newBuilder()
                .setTokenIndex(tokenDetail.getTokenIndex())
                .setTokenId(tokenId)
                .setTokenName(tokenDetail.getTokenName())
                .setTokenFullName(tokenDetail.getTokenFullName())
                .setMinPrecision(tokenDetail.getMinPrecision())
                .setAddressType("")
                .setDepositMinQuantity(DecimalUtil.toTrimString(tokenDetail.getDepositMinQuantity()))
                .setAllowDeposit(false)
                .setAllowWithdraw(false)
                .setIcon(tokenDetail.getIcon())
                .setTokenDetail("")
                .setBrokerId(brokerId)
                .setCategory(tokenDetail.getCategoryValue())
                .build();

        AddTokenReply tokenReply = getBrokerStub(brokerId).addToken(token);
        return tokenReply;
    }

    @Override
    public TokenPublishReply publishBrokerToken(long brokerId, String tokenId, int category, boolean published) {
        TokenPublishRequest request = TokenPublishRequest.newBuilder()
                .setTokenId(tokenId)
                .setPublished(published)
                .setBrokerId(brokerId)
                .build();
        TokenPublishReply reply = getBrokerStub(brokerId).tokenPublish(request);
        return reply;
    }

    @Override
    public AdminSimplyReply deleteBrokerToken(DeleteTokenRequest request) {
        return getBrokerStub(request.getOrgId()).deleteToken(request);
    }

    @Override
    public int changeTokenApplyBroker(String tokenId, Long exchangeId, Long toExchangeId, Long brokerId, Long toBrokerId) {
        ChangeTokenApplyBrokerReply result = getStub().changeTokenApplyBroker(ChangeTokenApplyBrokerRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setToExchangeId(toExchangeId)
                    .setBrokerId(brokerId)
                    .setToBrokerId(toBrokerId)
                .setTokenId(tokenId)
                .build());
        return result.getResult();
    }

    @Override
    public List<TokenDetail> getBhTokensByTokenIds(List<String> tokenIds) {
        if (CollectionUtils.isEmpty(tokenIds)) {
            return new ArrayList<>();
        }
        GetTokenIdsRequest.Builder builder = GetTokenIdsRequest.newBuilder()
            .addAllTokenIds(tokenIds);
        TokenList reply = getStub().getTokenListByIds(builder.build());
        List<TokenDetail> details = reply.getTokenDetailsList();
        return details;
    }

    @Override
    public QueryTokenSimpleReply queryBrokerSimpleTokens(long brokerId, int category) {
        AdminTokenServiceGrpc.AdminTokenServiceBlockingStub stub = getBrokerStub(brokerId);
        return stub.queryTokenSimple(QueryTokenSimpleRequest.newBuilder().setBrokerId(brokerId).setCategory(category).build());
    }

    @Override
    public QueryTokenReply queryBrokerTokens(Long brokerId, Integer current, Integer pageSize, Integer category, String tokenName) {
        if (category == null) {
            category = 0;
        }

        io.bhex.broker.grpc.admin.QueryTokenRequest request = io.bhex.broker.grpc.admin.QueryTokenRequest.newBuilder()
                .setCurrent(current)
                .setPageSize(pageSize)
                .setBrokerId(brokerId)
                .setCategory(category)
                .setTokenName(Strings.nullToEmpty(tokenName).toUpperCase())
                .build();

        return getBrokerStub(brokerId).queryToken(request);
    }


}
