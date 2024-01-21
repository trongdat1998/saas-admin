package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class VerifyFlowRecordDTO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    //private Long orgId;

    private Integer bizType;

    private String title;

    private String description;

    private Map<String, Object> verifyContent; //审核内容 用json表示，一般会有其它表来表示自己的业务

    private Integer status; //状态 0.初始化 1.审核中 2.结束-通过 3.结束-拒绝

    @JsonSerialize(using = ToStringSerializer.class)
    private Long adminUserId;

    private String adminUserName;

    private Long updatedAt;

    private Long createdAt;

    private int myVerifyLevel;

    private List<Map<String, String>> verifyUsers;

    private List<VerifyHistory> verifyHistories;

    @JsonIgnore
    private Integer currentVerifyLevel;

    @Data
    public static class VerifyHistory {
        //审核状态（1-通过 2-拒绝）
        private Integer verifyStatus;

        //审核意见
        private String reviewComments;

        private Long adminUserId;

        private String adminUserName;

        private Long verifyTime;
    }


 }
