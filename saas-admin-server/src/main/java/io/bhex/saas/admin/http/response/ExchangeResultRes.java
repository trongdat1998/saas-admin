package io.bhex.saas.admin.http.response;

import lombok.Data;

@Data
public class ExchangeResultRes<T> {
    private int status;
    private String err;
    private T data;
}
