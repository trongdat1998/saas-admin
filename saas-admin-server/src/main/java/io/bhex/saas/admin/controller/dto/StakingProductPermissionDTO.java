package io.bhex.saas.admin.controller.dto;

import lombok.Data;

@Data
public class StakingProductPermissionDTO {
    private Long brokerId;
    private String brokerName;
    private Integer allowFixed;
    private Integer allowFixedLock;
    private Integer allowCurrent;
}
