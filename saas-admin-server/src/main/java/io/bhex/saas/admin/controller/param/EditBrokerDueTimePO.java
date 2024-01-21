package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class EditBrokerDueTimePO {

    @NotNull(message = "brokerId not null")
    private Long id;


    private long dueTime = 0; //过期时间，0-代表未设置
}
