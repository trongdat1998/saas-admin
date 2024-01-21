package io.bhex.saas.admin.controller.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

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
public class EditBrokerRegisterOptionPO implements Serializable {

    @NotNull(message = "brokerId not null")
    private Long id;

    @Range(min = 1, max = 4, message = "registerOption is not in 1-4")
    private Integer registerOption;

}
