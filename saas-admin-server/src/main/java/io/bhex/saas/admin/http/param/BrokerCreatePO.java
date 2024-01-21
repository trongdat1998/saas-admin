package io.bhex.saas.admin.http.param;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.http.param
 * @Author: ming.xu
 * @CreateDate: 17/08/2018 12:10 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Builder
public class BrokerCreatePO {

    private Long brokerId;

    private String name;

    private String fullName;

    private String email;

    private String phone;

    private String host;

    private String earnestAddress;

    private String contact;

    private String basicInfo;

}
