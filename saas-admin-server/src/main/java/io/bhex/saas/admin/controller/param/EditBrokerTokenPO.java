package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
@Data
public class EditBrokerTokenPO {


    @NotNull
    private Long brokerId;

    @NotEmpty
    private String tokenId;

//    @NotNull
//    private Integer category;
}
