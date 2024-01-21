package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.bhex.bhop.common.util.locale.LocaleInputDeserialize;
import io.bhex.bhop.common.util.locale.LocaleOutputSerialize;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/30 下午5:34
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class OtcPaymentItemPO implements Serializable {

    private List<OtcPaymentItem> paymentItems;

    @Data
    public static class OtcPaymentItem implements Serializable {
        private Integer paymentType;
        @JsonSerialize(using = LocaleOutputSerialize.class)
        @JsonDeserialize(using = LocaleInputDeserialize.class)
        private String  language;
        private PaymentItem paymentItem;
    }

    @Data
    public static class PaymentItem implements Serializable {
        private Integer paymentType;
        private String  paymentName;
        private String  icon;
        private List<Item> items;
    }

    @Data
    public static class Item implements Serializable {
        private Boolean view;
        private String  name;
        private String label;
        private String placeholder;
        private String type;
        private String maxLength;
        private String required;
    }

}
