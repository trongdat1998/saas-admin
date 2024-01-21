package io.bhex.saas.admin.model;

import com.google.common.base.Strings;
import io.bhex.base.bhadmin.TokenApplyObj;
import io.bhex.bhop.common.constant.AdminTokenTypeEnum;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.broker.common.util.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.JdbcType;
import tk.mybatis.mapper.annotation.ColumnType;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Data
@Table(name = "tb_token_apply_record")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenApplyRecord {

    @Id
    private Long id;
    private Long exchangeId;
    private Long brokerId;
    private Integer tokenType;
    private Integer state;

    // Token settings
    private String tokenId;
    private String tokenName;
    private String tokenFullName;
    private BigDecimal fairValue;
    private String iconUrl;
    private String contractAddress;
    // length 2000
    private String introduction;

    // Withdraw/deposit settings
    private BigDecimal minDepositingAmt;
    private BigDecimal minWithdrawingAmt;
    private BigDecimal brokerWithdrawingFee;
    private BigDecimal maxWithdrawingAmt;

    private String reason;

    private String feeToken;
    private BigDecimal platformFee;
    private Integer needTag;
    private Integer confirmCount;
    private Integer canWithdrawConfirmCount;
    private Integer minPrecision;
    private String exploreUrl;

    @ColumnType(jdbcType = JdbcType.TIMESTAMP)
    private Date createAt;
    @ColumnType(jdbcType = JdbcType.TIMESTAMP)
    private Date updateAt;


    private String maxQuantitySupplied;
    private String currentTurnover;
    private String officialWebsiteUrl; //官网
    private String whitePaperUrl;
    private String publishTime;

    private Integer isPrivateToken;
    private Integer isBaas;
    private Integer isAggregate;
    private Integer isTest;
    private Integer isMainstream;
    private String extraTag; //json map<string,int>
    private String extraConfig; //json map<string,string>

    private String chainName;
    private String parentTokenId;
    private Integer chainSequence;


    public static TokenApplyRecord parseFromProtoObj(TokenApplyObj tokenRecord) {
        return TokenApplyRecord.builder()
                .id(tokenRecord.getId())
                .exchangeId(tokenRecord.getExchangeId())
                .brokerId(tokenRecord.getBrokerId())
                .tokenType(tokenRecord.getTokenType())
                .state(tokenRecord.getState())
                .tokenId(tokenRecord.getTokenId())
                .tokenName(tokenRecord.getTokenName()).tokenFullName(tokenRecord.getTokenFullName())
                .fairValue(DecimalUtil.toBigDecimal(tokenRecord.getFairValue()))
                .iconUrl(tokenRecord.getIconUrl())
                .contractAddress(tokenRecord.getContractAddress())
                .introduction(tokenRecord.getIntroduction())
                .minDepositingAmt(DecimalUtil.toBigDecimal(tokenRecord.getMinDepositingAmt()))
                .minWithdrawingAmt(DecimalUtil.toBigDecimal(tokenRecord.getMinWithdrawingAmt()))
                .brokerWithdrawingFee(DecimalUtil.toBigDecimal(tokenRecord.getBrokerWithdrawingFee()))
                .maxWithdrawingAmt(DecimalUtil.toBigDecimal(tokenRecord.getMaxWithdrawingAmt()))
                .reason(tokenRecord.getReason())
                .platformFee(DecimalUtil.toBigDecimal(tokenRecord.getPlatformFee()))
                .feeToken(tokenRecord.getFeeToken())
                .needTag(tokenRecord.getNeedTag())
                .confirmCount(tokenRecord.getConfirmCount())
                .canWithdrawConfirmCount(tokenRecord.getCanWithdrawConfirmCount())
                .minPrecision(tokenRecord.getMinPrecision())
                .exploreUrl(tokenRecord.getExploreUrl())
                .createAt(tokenRecord.getCreateAt() == 0 ? null : new Date(tokenRecord.getCreateAt()))
                .updateAt(tokenRecord.getUpdateAt() == 0 ? null : new Date(tokenRecord.getUpdateAt()))
                .maxQuantitySupplied(Strings.nullToEmpty(tokenRecord.getMaxQuantitySupplied()))
                .currentTurnover(Strings.nullToEmpty(tokenRecord.getCurrentTurnover()))
                .officialWebsiteUrl(tokenRecord.getOfficialWebsiteUrl())
                .whitePaperUrl(tokenRecord.getWhitePaperUrl())
                .publishTime(tokenRecord.getPublishTime())
                .isPrivateToken(tokenRecord.getIsPrivateToken() ? 1 : 0)
                .isAggregate(tokenRecord.getIsAggregate() ? 1 : 0)
                .isBaas(tokenRecord.getIsBaas() ? 1 : 0)
                .isMainstream(tokenRecord.getIsMainstream() ? 1 : 0)
                .extraTag(JsonUtil.defaultGson().toJson(tokenRecord.getExtraTagMap()))
                .extraConfig(JsonUtil.defaultGson().toJson(tokenRecord.getExtraConfigMap()))
                .build();
    }

    public static TokenApplyObj toProtoObj(TokenApplyRecord record) {
        if (record == null) {
            return TokenApplyObj.getDefaultInstance();
        }
        AdminTokenTypeEnum tokenTypeEnum = AdminTokenTypeEnum.getByType(record.getTokenType());
        return TokenApplyObj.newBuilder()
                .setId(record.getId())
                .setExchangeId(record.getExchangeId())
                .setBrokerId(record.getBrokerId())
                .setTokenType(record.getTokenType())
                .setState(record.getState())
                .setTokenId(record.getTokenId())
                .setTokenName(record.getTokenName())
                .setTokenFullName(record.getTokenFullName())
                .setFairValue(DecimalUtil.fromBigDecimal(record.getFairValue()))
                .setIconUrl(record.getIconUrl())
                .setContractAddress(record.getContractAddress())
                .setIntroduction(Strings.nullToEmpty(record.getIntroduction()))
                .setMinDepositingAmt(DecimalUtil.fromBigDecimal(record.getMinDepositingAmt()))
                .setMinWithdrawingAmt(DecimalUtil.fromBigDecimal(record.getMinWithdrawingAmt()))
                .setBrokerWithdrawingFee(DecimalUtil.fromBigDecimal(record.getBrokerWithdrawingFee()))
                .setMaxWithdrawingAmt(DecimalUtil.fromBigDecimal(record.getMaxWithdrawingAmt()))
                .setReason(Strings.nullToEmpty(record.getReason()))
                .setFeeToken(Strings.nullToEmpty(record.getFeeToken()))
                .setPlatformFee(DecimalUtil.fromBigDecimal(record.getPlatformFee() == null
                        ? new BigDecimal(tokenTypeEnum.getPlatformFee()) : record.getPlatformFee()))
                //.setNeedTag(this.needTag == null ? (tokenTypeEnum.isNeedTag() ? 1 : 0) : this.needTag)
                .setNeedTag(tokenTypeEnum.isNeedTag() ? 1 : 0)
                .setConfirmCount(record.getConfirmCount() == null ? tokenTypeEnum.getConfirmCount() : record.getConfirmCount())
                .setCanWithdrawConfirmCount(record.getCanWithdrawConfirmCount() == null
                        ? tokenTypeEnum.getCanWithdrawConfirmCount() : record.getCanWithdrawConfirmCount())
                .setMinPrecision(record.getMinPrecision() == null ? tokenTypeEnum.getMinPrecision() : record.getMinPrecision())
                .setExploreUrl(record.getExploreUrl() == null ? tokenTypeEnum.getExploreUrl() : record.getExploreUrl())
                .setCreateAt(record.getCreateAt().getTime())
                .setUpdateAt(record.getUpdateAt().getTime())
                .setMaxQuantitySupplied(Strings.nullToEmpty(record.getMaxQuantitySupplied()))
                .setCurrentTurnover(Strings.nullToEmpty(record.getCurrentTurnover()))
                .setOfficialWebsiteUrl(Strings.nullToEmpty(record.getOfficialWebsiteUrl()))
                .setWhitePaperUrl(Strings.nullToEmpty(record.getWhitePaperUrl()))
                .setPublishTime(Strings.nullToEmpty(record.getPublishTime()))
                .setIsPrivateToken(record.getIsPrivateToken() == 1)
                .setIsBaas(record.getIsBaas() == 1)
                .setIsAggregate(record.getIsAggregate() == 1)
                .setIsTest(record.getIsTest() == 1)
                .putAllExtraTag(JsonUtil.defaultGson().fromJson(record.getExtraTag(), Map.class))
                .putAllExtraConfig(JsonUtil.defaultGson().fromJson(record.getExtraConfig(), Map.class))
                .setChainName(record.getChainName() == null ? "" : record.getChainName())
                .setParentTokenId(record.getParentTokenId() == null ? "" : record.getParentTokenId())
                .setChainSequence(record.getChainSequence() == null ? 0 : record.getChainSequence())
                .build();
    }


}
