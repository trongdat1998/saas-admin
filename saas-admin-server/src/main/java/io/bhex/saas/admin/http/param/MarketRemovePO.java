package io.bhex.saas.admin.http.param;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MarketRemovePO {

    private Long exchangeId;
    private String symbolId;
    private String remark;

}
