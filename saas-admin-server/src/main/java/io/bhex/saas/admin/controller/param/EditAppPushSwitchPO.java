package io.bhex.saas.admin.controller.param;



import io.bhex.bhop.common.util.validation.StringInValid;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Description:
 * @Date: 2019/6/13 下午4:29
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class EditAppPushSwitchPO {
    @NotNull
    private Long orgId;

    @StringInValid(value = {"ALL_SITE", "CUSTOM"})
    private String switchType;

    private Boolean open = false;

}
