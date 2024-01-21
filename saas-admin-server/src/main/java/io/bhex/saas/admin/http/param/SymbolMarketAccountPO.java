package io.bhex.saas.admin.http.param;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolMarketAccountPO {

    private Long orgId;

    private List<SymbolMarketAccountDetailPO> symbolMarketAccountList;
}
