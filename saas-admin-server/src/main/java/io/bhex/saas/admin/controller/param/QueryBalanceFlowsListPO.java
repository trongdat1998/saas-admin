package io.bhex.saas.admin.controller.param;

import io.bhex.bhop.common.util.validation.IntInValid;
import lombok.Data;

@Data
public class QueryBalanceFlowsListPO {

    private Long orgId;

    private Long userId;

    private String nationalCode;

    private String phone;

    private String email;

    private Long fromId;

    private Long lastId;

    private Boolean next;

    private Integer pageSize;

    private Integer businessSubject;

    private String tokenId;

    @IntInValid( value = {0, 1, 4}, allowZero = true)
    private Integer category = 0; //TokenCategory

}
