package io.bhex.saas.admin.controller.dto;

import lombok.Data;


@Data
public class OtcPaymentItemDTO {
    /**
     * 支付方式
     */
    private Integer paymentType;

    /**
     * 支付方式要素项
     */
    private String paymentItems;

    /**
     * 语言
     */
    private String language;
}
