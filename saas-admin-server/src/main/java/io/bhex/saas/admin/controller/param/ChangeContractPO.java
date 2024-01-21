package io.bhex.saas.admin.controller.param;

import lombok.Data;

/**
 * @ProjectName: exchange
 * @Package: io.bhex.ex.admin.dto.param
 * @Author: ming.xu
 * @CreateDate: 18/09/2018 10:42 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class ChangeContractPO {

    private Long brokerId;

    private Long exchangeId;

    private Boolean bind;

    private Boolean trust;
}
