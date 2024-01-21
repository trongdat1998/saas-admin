package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class EditBrokerSymbolPO implements Serializable {
//    @NotNull
//    private Long exchangeId;

    @NotNull
    private Long brokerId;


    @NotEmpty
    private String symbolId;
}
