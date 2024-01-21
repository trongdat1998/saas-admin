package io.bhex.saas.admin.http.param;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class SaveMarketAccountPO {
    @NotNull
    private Long orgId;

    private Long accountId;

    private String symbolId;

    private String takerBuyFee;

    private String takerSellFee;

    private String makerBuyFee;

    private String makerSellFee;
}
