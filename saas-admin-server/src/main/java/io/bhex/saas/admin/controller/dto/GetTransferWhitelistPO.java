package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 获取转账白名单
 */
@Data
public class GetTransferWhitelistPO implements Serializable {

    @NotNull(message = "brokerId not null")
    private Long brokerId;

}
