package io.bhex.saas.admin.controller.param;

import lombok.Data;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.param
 * @Author: ming.xu
 * @CreateDate: 18/09/2018 5:04 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class InstanceInfoPO {

    private Long exchangeId;
    private Long brokerId;
    private String brokerName;

}
