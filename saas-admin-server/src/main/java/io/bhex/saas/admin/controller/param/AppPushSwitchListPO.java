package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @Description:创建短信签名
 * @Date: 2018/10/10 上午11:17
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class AppPushSwitchListPO {

    private Long orgId = 0L;

    private Long lastId;

    private Integer pageSize;
}
