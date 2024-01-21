package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Description:
 * @Date: 2020/3/10 下午2:58
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@Table(name = "tb_verify_flow_config")
public class VerifyFlowConfig {

    @Id
    private Long id;

    private Long orgId;

    //业务类型
    private Integer bizType;

    //审核等级
    private Integer level;
    
    //审批人 uid用,分隔
    private String verifyUserIds;

    private Integer canClose;

    private Integer canChange;

    private Integer status;
    
    private Long adminUserId;
    
    private String adminUserName;
    
    private Long createdAt;

    private Long updatedAt;
}
