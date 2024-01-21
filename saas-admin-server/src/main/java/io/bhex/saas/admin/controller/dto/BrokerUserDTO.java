package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @Description: broker C端用户数据
 * @Date: 2018/8/23 下午8:15
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class BrokerUserDTO {


    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId = 0L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long accountId = 0L;

    private String firstName;
    private String secondName;
    private String nationalCode;

    private String mobile;

    private String email;

    private Boolean bindGa;
    //注册方式, 1 手机号 2 邮箱
    //private int registerType;

    //private int userType;

    //kyc 状态：1 审核中  2 审核通过 3 审核未通过，重新上传
    private int verifyStatus;

    private long created;

    //1,"启用"  2,"禁用"
    private int userStatus;

    @JsonIgnore
    private String realEmail;
    @JsonIgnore
    private String realMobile;

    private Integer isFreezeLogin;

    private Long inviteUserId;
    //private Long defaultAccountId;

    private Integer agentLevel; //经记人 等级
}
