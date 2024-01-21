package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_verify_flow_history")
public class VerifyFlowHistory {

    @Id
    private Long id;

    private Long orgId;

    private Long bizRecordId;

    //审核状态（1-通过 2-拒绝）
    private Integer verifyStatus;

    //审核意见
    private String reviewComments;

    private Long adminUserId;

    private String adminUserName;

    private Long verifyTime;

    private Long updatedAt;

}
