package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 05/09/2018 5:36 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class BrokerIdPO {

    @NotNull(message = "brokerId not null")
    private Long id;
}
