package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Date: 2018/10/10 上午11:33
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class BrokerIdPO {
    @NotNull
    private Long brokerId;
}
