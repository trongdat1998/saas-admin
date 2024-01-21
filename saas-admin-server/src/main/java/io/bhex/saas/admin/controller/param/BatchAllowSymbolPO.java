package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class BatchAllowSymbolPO {


    @NotNull
    private List<Long> brokers;

    @NotNull
    private List<String> symbols;

    @NotNull
    private Integer category;

}
