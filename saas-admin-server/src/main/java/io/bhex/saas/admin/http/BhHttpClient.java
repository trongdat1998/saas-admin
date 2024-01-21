package io.bhex.saas.admin.http;

import feign.Headers;
import feign.RequestLine;
import io.bhex.saas.admin.controller.param.BrokerAccountTradeFeePO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

public interface BhHttpClient {

    @Data
    @NoArgsConstructor
    class TradeFeeAdjustPO  implements Serializable {
        private Long brokerId;
        private Long lastId;
        private Integer pageSize;
    }

    @RequestLine("POST /internal/common/accountTradeFee/set")
    @Headers("Content-Type: application/json")
    String changeBrokerTradeFee(BrokerAccountTradeFeePO param);

    @RequestLine("POST /internal/common/accountTradeFee/listZeroTakerFeeAdjust")
    @Headers("Content-Type: application/json")
    String listZeroTakerFeeAdjust(TradeFeeAdjustPO param);

}
