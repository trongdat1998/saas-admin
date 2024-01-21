package io.bhex.saas.admin.controller.dto;

import io.bhex.bhop.common.util.validation.CommonInputValid;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 05/09/2018 4:37 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class EditBrokerPO implements Serializable {

    @NotNull(message = "brokerId not null")
    private Long id;

    //@NotNull(message = "saasFeeRate not null")
    //private BigDecimal saasFeeRate;

    @NotNull(message = "email not null")
    private String email;

    @NotNull(message = "company not null")
    private String company;

    @NotNull(message = "contact not null")
    private String contact;

    @NotNull(message = "phone not null")
    private String phone;

    private String basicInfo;

    @CommonInputValid
    private String name;
    @CommonInputValid
    private String apiDomain;

    //下面的两个参数用于调用brokeradmin使用
    private Long brokerId;
    private String brokerName;
    private boolean enable = true;

    private long dueTime = 0; //过期时间，0-代表未设置
}
