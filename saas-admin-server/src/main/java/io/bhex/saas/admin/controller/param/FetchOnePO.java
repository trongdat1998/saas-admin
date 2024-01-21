package io.bhex.saas.admin.controller.param;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class FetchOnePO {

    @NotNull
    private String namespace;

    @NotNull
    private String dbName;

    @NotBlank
    private String tableName;

    private String fields = "*";

    /**
     * 支持简单的大于等于的方式，值不允许变量或者是内置mysql函数比如unix_timestamp()
     */
    private String[] conditions = new String[0];

}
