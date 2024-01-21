package io.bhex.saas.admin.http.param;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MarketAddPO {


    private String partitionName;

    private Long exchangeId;

    private String symbolId;

    private String symbolName;


    private String remark;

    private String topicName;

    private List<Integer> dumpScales;

    private Boolean isReverse;

    private Long firstReqTime;

}
