package io.bhex.saas.admin.controller.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller.param
 * @Author: ming.xu
 * @CreateDate: 2019/10/23 5:43 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationListPO {

    private Integer current = 1;

    private Integer pageSize = 30;

    @NotNull
    private Integer state = 0;
}
