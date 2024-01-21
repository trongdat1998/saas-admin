package io.bhex.saas.admin.controller.dto;

import lombok.Data;

@Data
public class AppPushSwitchDTO {

    private Long orgId;

    private String orgName;

    private boolean allSiteSwitchOpen;

    private boolean customSwitchOpen;

}
