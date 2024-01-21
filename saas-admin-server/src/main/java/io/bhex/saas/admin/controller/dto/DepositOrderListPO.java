package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-12-15 15:18
 */
@Data
public class DepositOrderListPO {
    @NotNull
    private Long orgId;

    private Long userId = 0L;

    private Long fromId = 0L;

    private Long lastId = 0L;

    private Boolean next = false;

    private Integer pageSize = 30;

    private String tokenId = "";

    private Long startTime = 0L;

    private Long endTime = 0L;

    private String address = "";

    private String txId = "";

    private Long orderId = 0L;

    private Integer receiptType = 0; // 入账类型

}
