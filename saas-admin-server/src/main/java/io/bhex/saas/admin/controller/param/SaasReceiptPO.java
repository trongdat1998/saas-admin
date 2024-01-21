package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-16 14:35
 */
@Data
public class SaasReceiptPO {
    @NotNull
    public Long orgId;
    public String brokerName;
    @NotEmpty
    public String tokenId;
    @NotEmpty
    public String amount;

    public Long userId;
    @NotNull
    public Long accountId;
    @NotEmpty
    public String address;
    public String tag;
    @NotNull
    public Long orderId;
    private String remark;
    private Integer receiptResult = 0 ; //未入账原因

}
