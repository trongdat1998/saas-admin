package io.bhex.saas.admin.controller.param;

import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;

@Data
public class QueryTokenApplicationsPO {


    @NotNull
    private Integer state;

    private Long exchangeId = 0L;

    private Long brokerId = 0L;

    @NotNull
    private Integer current = 1;

    @NotNull
    private Integer pageSize = 100;


    private String contractAddress = "";

    private String token = null;

}
