package io.bhex.saas.admin.controller.dto;

import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.TokenDetailInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDTO implements Serializable {

    //币种Id
    private String tokenId;

    //币种名称
    private String tokenName;

    //币种全名
    private String tokenFullName;

    private String description;

    private Integer minPrecision;

    private BigDecimal withdrawMinQuantity;

    private BigDecimal depositMinQuantity;

    private String icon;

    private String tokenDetail;

    private Boolean addressNeedTag;

    private Integer chainConfirmCount;

    private Integer canWithdrawConfirmCount;

    private String exploreUrl;

    private BigDecimal platformFee;

    private String feeToken;

    private BigDecimal safeQuantity;

    public static TokenDTO parseTokenDetail(TokenDetailInfo tokenDetail) {
        return TokenDTO.builder()
            .tokenId(tokenDetail.getTokenId())
            .tokenName(tokenDetail.getTokenId())
            .tokenFullName(tokenDetail.getTokenFullName())
            .description(tokenDetail.getTokenDetail())
            .minPrecision(tokenDetail.getMinPrecision())
            .withdrawMinQuantity(DecimalUtil.toBigDecimal(tokenDetail.getWithdrawMinQuantity()))
            .depositMinQuantity(DecimalUtil.toBigDecimal(tokenDetail.getDepositMinQuantity()))
            .icon(tokenDetail.getIcon())
            .tokenDetail(tokenDetail.getTokenDetail())
            .addressNeedTag(tokenDetail.getAddressNeedTag())
            .chainConfirmCount(tokenDetail.getChainConfirmCount())
            .canWithdrawConfirmCount(tokenDetail.getCanWithdrawConfirmCount())
            .exploreUrl(tokenDetail.getExploreUrl())
            .platformFee(DecimalUtil.toBigDecimal(tokenDetail.getPlatformFee()))
            .feeToken(tokenDetail.getFeeToken())
            .safeQuantity(DecimalUtil.toBigDecimal(tokenDetail.getSafeQuantity()))
            .build();
    }

}
