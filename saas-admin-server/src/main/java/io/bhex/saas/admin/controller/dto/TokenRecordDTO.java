package io.bhex.saas.admin.controller.dto;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;
import io.bhex.base.bhadmin.TokenApplyObj;
import io.bhex.base.exadmin.TokenRecord;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.quote.FairValue;
import io.bhex.base.token.TokenDetailInfo;
import io.bhex.bhop.common.util.locale.LocaleOutputSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRecordDTO {
    private Long id;
    // ETH
    private Long exchangeId;
    private Long brokerId;
    private String orgName;
    private Integer tokenType;
    private String tokenId;
    private String tokenName;
    private String tokenFullName;
    private Integer state;
    private BigDecimal fairValue;
    private String iconUrl;
    private String contractAddress;
    private String introduction;

    private BigDecimal minDepositingAmt;
    private BigDecimal minWithdrawingAmt;

    private String reason;

    private Integer feeToken;
    private BigDecimal platformFee;
    private String exploreUrl;
    private Integer needTag;
    private Integer confirmCount;
    private Integer canWithdrawConfirmCount;
    private Integer minPrecision;

    private Long createAt;
    private Long updateAt;

    private String maxQuantitySupplied;
    private String currentTurnover;
    private String officialWebsiteUrl; //官网
    private String whitePaperUrl;
    private String publishTime;
    private Boolean isPrivateToken = false;
    private Boolean isBaas = false;
    private Boolean isAggregate = false;
    private Boolean isTest = false;

    private String chainName;
    private String parentTokenId;
    private Integer chainSequence;

    private List<IntroductionDTO> introductions = new ArrayList<>();

    public static TokenRecordDTO parseTokenDetail(TokenDetailInfo tokenDetail,
                                                  FairValue fairValue,
                                                  TokenApplyObj tokenRecord) {
        List<IntroductionDTO> theIntroductions = new ArrayList<>();
        String introduction = tokenDetail.getTokenDetail();
        if (!introduction.startsWith("[") || !introduction.endsWith("]")) { //兼容原有内容，不是json数组结构的
            theIntroductions.add(new IntroductionDTO(introduction, "zh_CN", true));
        } else {
            theIntroductions = JSON.parseArray(introduction, IntroductionDTO.class);
        }

        int feeToken = getFeeTokenValue(tokenRecord.getFeeToken());
        return TokenRecordDTO.builder()
            .exchangeId(fairValue.getExchangeId())
                .brokerId(tokenRecord.getBrokerId())
            .tokenId(tokenDetail.getTokenId())
            .tokenName(tokenDetail.getTokenFullName()).tokenFullName(tokenDetail.getTokenFullName())
            .tokenType(tokenDetail.getTokenTypeValue())
            .iconUrl(tokenDetail.getIcon())
            .introductions(theIntroductions)
            .minDepositingAmt(DecimalUtil.toBigDecimal(tokenDetail.getDepositMinQuantity()))
            .minWithdrawingAmt(DecimalUtil.toBigDecimal(tokenDetail.getWithdrawMinQuantity()))
            .feeToken(feeToken)
            .platformFee(DecimalUtil.toBigDecimal(tokenDetail.getPlatformFee()))
            .exploreUrl(tokenDetail.getExploreUrl())
            .needTag(tokenDetail.getAddressNeedTag() ? 1 : 0)
            .confirmCount(tokenDetail.getChainConfirmCount())
            .canWithdrawConfirmCount(tokenDetail.getCanWithdrawConfirmCount())
            .minPrecision(tokenDetail.getMinPrecision())
            .fairValue(DecimalUtil.toBigDecimal(fairValue.getUsd()))
            .contractAddress(tokenRecord.getContractAddress())

            .maxQuantitySupplied(tokenDetail.getMaxQuantitySupplied())
            .currentTurnover(tokenDetail.getCurrentTurnover())
            .officialWebsiteUrl(tokenDetail.getOfficialWebsiteUrl())
            .whitePaperUrl(tokenDetail.getWhitePaperUrl())
            .publishTime(tokenDetail.getPublishTime())
                .isPrivateToken(tokenRecord.getIsPrivateToken())
            .isAggregate(tokenRecord.getIsAggregate())
                .isBaas(tokenRecord.getIsBaas())
                .isTest(tokenRecord.getIsTest())
            .build();
    }

    public static final String ETH = "ETH";
    public static final String EOS = "EOS";
    public static final String TRX = "TRX";

    public static String getFeeTokenByValue(String tokenId, int feeTokenValue, String parentTokenId) {
        if (!Strings.isNullOrEmpty(parentTokenId)) {
            return parentTokenId;
        }
        String feeToken = tokenId;
        if (feeTokenValue == 1) {
            feeToken = EOS;
        } else if (feeTokenValue == 0) {
            feeToken = ETH;
        } else if (feeTokenValue == 3) {
            feeToken = TRX;
        }
        return feeToken;
    }

    private static int getFeeTokenValue(String feeTokenStr) {
        int feeToken = 2;
        if (ETH.equalsIgnoreCase(feeTokenStr)) {
            feeToken = 0;
        } else if (EOS.equalsIgnoreCase(feeTokenStr)) {
            feeToken = 1;
        } else if (TRX.equalsIgnoreCase(feeTokenStr)) {
            feeToken = 3;
        }
        return feeToken;
    }

    public static TokenRecordDTO parseTokenRecord(TokenApplyObj tokenRecord) {
        List<IntroductionDTO> theIntroductions = new ArrayList<>();
        String introduction = tokenRecord.getIntroduction();
        if (!introduction.startsWith("[") || !introduction.endsWith("]")) { //兼容原有内容，不是json数组结构的
            theIntroductions.add(new IntroductionDTO(introduction, "zh_CN", true));
        } else {
            theIntroductions = JSON.parseArray(introduction, IntroductionDTO.class);
        }

        int feeToken = getFeeTokenValue(tokenRecord.getFeeToken());
        return TokenRecordDTO.builder()
            .id(tokenRecord.getId())
            .exchangeId(tokenRecord.getExchangeId())
                .brokerId(tokenRecord.getBrokerId())
            .tokenType(tokenRecord.getTokenType())
            .tokenId(tokenRecord.getTokenId())
            .tokenName(tokenRecord.getTokenName()).tokenFullName(tokenRecord.getTokenFullName())
            .state(tokenRecord.getState())
            .fairValue(DecimalUtil.toBigDecimal(tokenRecord.getFairValue()))
            .iconUrl(tokenRecord.getIconUrl())
            .contractAddress(tokenRecord.getContractAddress())
            .introductions(theIntroductions)
            .minDepositingAmt(DecimalUtil.toBigDecimal(tokenRecord.getMinDepositingAmt()))
            .minWithdrawingAmt(DecimalUtil.toBigDecimal(tokenRecord.getMinWithdrawingAmt()))
            .reason(tokenRecord.getReason())
            .feeToken(feeToken)
            .platformFee(DecimalUtil.toBigDecimal(tokenRecord.getPlatformFee()))
            .exploreUrl(tokenRecord.getExploreUrl())
            .needTag(tokenRecord.getNeedTag())
            .confirmCount(tokenRecord.getConfirmCount())
            .canWithdrawConfirmCount(tokenRecord.getCanWithdrawConfirmCount())
            .minPrecision(tokenRecord.getMinPrecision())
            .createAt(tokenRecord.getCreateAt())
            .updateAt(tokenRecord.getUpdateAt())
            .maxQuantitySupplied(Strings.nullToEmpty(tokenRecord.getMaxQuantitySupplied()))
            .currentTurnover(Strings.nullToEmpty(tokenRecord.getCurrentTurnover()))
            .officialWebsiteUrl(tokenRecord.getOfficialWebsiteUrl())
            .whitePaperUrl(tokenRecord.getWhitePaperUrl())
            .publishTime(tokenRecord.getPublishTime())
                .isPrivateToken(tokenRecord.getIsPrivateToken())
                .isAggregate(tokenRecord.getIsAggregate())
                .isBaas(tokenRecord.getIsBaas())
                .isTest(tokenRecord.getIsTest())
                .chainName(tokenRecord.getChainName())
                .parentTokenId(tokenRecord.getParentTokenId())
                .chainSequence(tokenRecord.getChainSequence())
            .build();
    }

    @Data
    @AllArgsConstructor
    public static class IntroductionDTO {

        @NotNull
        private String content;

        @NotNull
        @JsonSerialize(using = LocaleOutputSerialize.class)
        private String language;

        private Boolean enable;

    }
}
