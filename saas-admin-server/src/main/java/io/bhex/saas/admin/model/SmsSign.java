package io.bhex.saas.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @Description: 短信签名
 * @Date: 2018/10/10 上午10:06
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
@Table(name = "tb_sms_sign")
public class SmsSign {

    @Id
    private Long id;

    @JsonProperty("brokerId")
    private Long orgId;

    @JsonProperty("brokerName")
    private String orgName;

    private String sign;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String language;

    @JsonIgnore
    private Integer deleted;//逻辑删除: 1=删除 0=正常
    @JsonIgnore
    private Timestamp createdAt;
    @JsonIgnore
    private Timestamp updatedAt;


}
