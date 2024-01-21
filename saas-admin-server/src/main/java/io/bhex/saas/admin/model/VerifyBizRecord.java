package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_verify_biz_record")
public class VerifyBizRecord {

    public static final int INIT_STATUS = 0;
    public static final int VERIFING_STATUS = 1;

    public static final int PASSED_STATUS = 2;
    public static final int PASSED_END_STATUS = 21;

    public static final int REJECTED_STATUS = 3;
    public static final int REJECTED_END_STATUS = 31;

    public static final int FAILED_STATUS = 4;
    public static final int FAILED_END_STATUS = 41;


    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    private Long orgId;

    private Integer bizType;

    private String title;

    private String description;

    private String verifyContent; //审核内容 用json表示，一般会有其它表来表示自己的业务

    private Integer status; //状态 0.初始化 1.审核中 2.结束-通过 3.结束-拒绝


    private Integer currentVerifyLevel;

    private Long adminUserId;

    private String adminUserName;

    private Long updatedAt;

    private Long createdAt;

    //审批人 uid用,分隔
    private String verifyUserIds;

}
