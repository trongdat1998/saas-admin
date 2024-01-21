package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bhex.bhop.common.util.locale.LocaleInputDeserialize;
import io.bhex.bhop.common.util.percent.PercentageOutputSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenAuditPO {
    @NotNull(message = "token.record.feeToken.required")
    private Integer feeToken;
    @NotNull(message = "token.record.platformFee.required")
    private BigDecimal platformFee;
    @NotNull(message = "token.record.minPrecision.required")
    private Integer minPrecision;
    @NotNull(message = "token.record.confirmCount.required")
    private Integer confirmCount;
//    @NotNull(message = "token.record.canWithdrawConfirmCount.required")
    private Integer canWithdrawConfirmCount;
    //@NotEmpty(message = "token.record.exploreUrl.required")
    private String exploreUrl;
    @NotNull(message = "token.record.needTag.required")
    private boolean needTag;
    @NotNull(message = "audit.curState.required")
    private Integer curState;
    @NotNull(message = "audit.toState.required")
    private Integer toState;
    @NotNull(message = "audit.id.required")
    private Long id;

    @NotNull(message = "token.record.tokenType.required")
    private Integer tokenType;
    @NotEmpty(message = "token.record.tokenId.required")
    private String tokenId;
    @NotEmpty(message = "token.record.tokenName.required")
    private String tokenName;
    @NotEmpty(message = "token.record.tokenFullName.required")
    private String tokenFullName;
    @JsonSerialize(using = PercentageOutputSerialize.class)
    @NotNull(message = "token.record.fairValue.required")
    private BigDecimal fairValue;
    @NotEmpty(message = "token.record.icoUrl.required")
    private String iconUrl;
    private String contractAddress;

    @JsonSerialize(using = PercentageOutputSerialize.class)
    @NotNull(message = "token.record.minDepositingAmt.required")
    private BigDecimal minDepositingAmt;
    @JsonSerialize(using = PercentageOutputSerialize.class)
    @NotNull(message = "token.record.minWithdrawingAmt.required")
    private BigDecimal minWithdrawingAmt;

    private String parentTokenId = "";

    private String chainName = "";

    // 链顺序，越小排序越靠前
    private Integer chainSequence = 0;

    private String reason;


    private String maxQuantitySupplied;


    private String currentTurnover;

    private String officialWebsiteUrl; //官网

    private String whitePaperUrl;

    private String publishTime;

    private Boolean isPrivateToken = false;

    private Boolean isBaas = false;
    private Boolean isAggregate = false;
    private Boolean isTest = false;
    private Boolean isMainstream = false;
    private List<IntroductionPO> introductions = new ArrayList<>();

    @Data
    public static class IntroductionPO {
        @NotNull
        private String content;

        @NotNull
        @JsonDeserialize(using = LocaleInputDeserialize.class)
        private String language;

        private Boolean enable;
    }
}
