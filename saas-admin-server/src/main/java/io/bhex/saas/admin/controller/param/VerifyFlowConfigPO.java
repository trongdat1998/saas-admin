package io.bhex.saas.admin.controller.param;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Date: 2020/3/10 下午2:58
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class VerifyFlowConfigPO {

    //业务类型
    private Integer bizType;

    //审核等级
    private Integer level;
    
    //审批人 uid用,分隔
    private List<Long> verifyUserIds;

    private Boolean canClose;

    private Boolean canChange;

}
