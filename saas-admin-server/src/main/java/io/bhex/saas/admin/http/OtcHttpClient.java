package io.bhex.saas.admin.http;

import feign.Headers;
import feign.RequestLine;
import io.bhex.bhop.common.dto.param.OrgIdPO;
import io.bhex.saas.admin.controller.param.UpdateOtcPaymentItemPO;

public interface OtcHttpClient {


    @RequestLine("POST /internal/share/addShareBroker")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    String addShareBroker(OrgIdPO orgIdPO);

    @RequestLine("POST /internal/share/cancelShareBroker")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    String cancelShareBroker(OrgIdPO orgIdPO);


    @RequestLine("POST /internal/share/list")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    String listShareBrokers();

    @RequestLine("POST /internal/payment_items/query")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    String queryPaymentItems();

    @RequestLine("POST /internal/payment_items/set")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    String setPaymentItems(UpdateOtcPaymentItemPO updatePo);


}
