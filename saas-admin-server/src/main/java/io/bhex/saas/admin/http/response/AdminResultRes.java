package io.bhex.saas.admin.http.response;

import lombok.Data;

@Data
public class AdminResultRes<T> {
    private Integer code;

    private String msg;

    private T data;
}
