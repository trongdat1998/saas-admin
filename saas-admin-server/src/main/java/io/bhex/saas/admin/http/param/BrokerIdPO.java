package io.bhex.saas.admin.http.param;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class BrokerIdPO {

    @NotNull
    private Long brokerId;
}
