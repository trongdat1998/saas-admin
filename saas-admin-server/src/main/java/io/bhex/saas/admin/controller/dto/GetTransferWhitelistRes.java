package io.bhex.saas.admin.controller.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 获取转账白名单
 */
@Data
@Builder
public class GetTransferWhitelistRes implements Serializable {

    private String language = "zh_CN";

    private String iniName;

    private String iniValue;

    private String iniDesc;

}
