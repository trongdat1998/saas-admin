package io.bhex.saas.admin.http;

import lombok.Data;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.http
 * @Author: ming.xu
 * @CreateDate: 17/08/2018 12:03 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class BrokerResultBean<T> {

    private int code;

    private String msg;

    private T data;
}
