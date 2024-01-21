package io.bhex.saas.admin.http;

import feign.Headers;
import feign.RequestLine;
import io.bhex.saas.admin.http.param.MarketAddPO;
import io.bhex.saas.admin.http.param.MarketRemovePO;
import io.bhex.saas.admin.http.param.NewExchangePO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;

import java.util.Map;

/**
 * @Description: 调用交易所http简单封装
 * @Date: 2018/8/16 上午10:34
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface ExchangeGatewayHttpClient {

    @RequestLine("POST /v1/gateway/exchange/new")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    ExchangeResultRes<Object> newExchange(NewExchangePO newExchangePO);


    @RequestLine("POST /v1/gateway/market/add")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    ExchangeResultRes<Map<String, Long>> marketAdd(MarketAddPO marketAddPO);


    @RequestLine("POST /v1/gateway/market/remove")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    ExchangeResultRes<Map<String, Long>> marketRemove(MarketRemovePO marketRemovePO);

}
