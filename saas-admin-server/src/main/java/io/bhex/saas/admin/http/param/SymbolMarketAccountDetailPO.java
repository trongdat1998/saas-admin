package io.bhex.saas.admin.http.param;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolMarketAccountDetailPO {

    private Long id;

    private Long orgId;

    private Long accountId;

    private String symbolId;

    private String takerBuyFee;

    private String takerSellFee;

    private String makerBuyFee;

    private String makerSellFee;
}
