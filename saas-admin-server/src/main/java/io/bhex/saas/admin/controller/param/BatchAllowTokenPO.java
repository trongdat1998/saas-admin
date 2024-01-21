package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchAllowTokenPO {


    @NotNull
    private List<Long> brokers;

    @NotNull
    private List<String> tokens;

    @NotNull
    private Integer category;
}
