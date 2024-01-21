package io.bhex.saas.admin.controller.dto;

import io.bhex.bhop.common.util.validation.CommonInputValid;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 设置转账白名单列表请求
 */
@Data
public class SetTransferWhitelistPO implements Serializable {

    @NotNull(message = "content not null")
    private String content;

    @NotNull(message = "brokerId not null")
    private Long brokerId;

}
