package io.bhex.saas.admin.controller.dto;

import io.bhex.base.quote.IndexConfig;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndexConfigDTO {
    private Long id;
    private String name;
    private Integer strategy;
    private String formula;

    public static IndexConfigDTO parseProto(IndexConfig indexConfig) {
        return IndexConfigDTO.builder()
            .id(indexConfig.getId())
            .name(indexConfig.getName())
            .formula(indexConfig.getFormula())
            .strategy(indexConfig.getStrategy())
            .build();
    }
}
