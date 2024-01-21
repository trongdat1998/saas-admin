package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-15 15:15
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long orderId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId;

    private String tokenId;

    private String tokenName;

    private String address;

    private String fromAddress;

    private String tokenQuantity;

    private String txid;

    private String statusCode;

    private String statusDesc;

    private Long time;
    private Long createTime;
    private String addressExt;

    private String fromAddressExt;
    /**
     * 入账类型
     */
    private Integer receiptResult;


    private Boolean isBaas = false;

    private Long orgId;
    //目标确认数
    private Integer targetConfirmNum;
    ///确认数
    private Integer confirmNum;
}
