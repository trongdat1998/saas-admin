package io.bhex.saas.admin.controller.param;

import lombok.Data;

@Data
public class GetBrokerUserPO {

    private Long orgId;

    private Long userId;

    private Long accountId;

    private String nationalCode;

    private String phone;

    private String email;



}
