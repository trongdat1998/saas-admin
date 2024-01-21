package io.bhex.saas.admin.controller.param;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexConfigPO {
    private Long id;
    @NotEmpty(message = "quote.data.index.formula")
    private String formula;
    private Integer strategy;
    @NotEmpty(message = "quote.data.index.name")
    private String name;
}
