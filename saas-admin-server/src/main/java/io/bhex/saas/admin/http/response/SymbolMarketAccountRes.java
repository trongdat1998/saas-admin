package io.bhex.saas.admin.http.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolMarketAccountRes {

    private Long id;

    private Long orgId;

    private String accountId;

    private String symbolId;

    private Integer category;

    private Long exchangeId;

    private String makerBuyFeeRate;

    private String makerSellFeeRate;

    private String takerBuyFeeRate;

    private String takerSellFeeRate;
}
