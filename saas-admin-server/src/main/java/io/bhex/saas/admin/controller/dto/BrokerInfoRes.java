package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 05/09/2018 3:20 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class BrokerInfoRes {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long brokerId;

    private String apiDomain;

    private Long instanceId;
    private String name;
    private String company;
    private String email;
    private String phone;
    //private String host;
    private String contact;
    private String basicInfo;
    //private BigDecimal saasFeeRate;
    private Boolean enabled;

    private int dnsSetting;

    private String adminWebUrl;

    private Long createdAt;

    private boolean forbidAccessStatus;

    private boolean otcShare;

    private Integer registerOption;

    private Long dueTime; //过期时间，0-代表未设置

    private Boolean verifying; //是否在审核中
}
