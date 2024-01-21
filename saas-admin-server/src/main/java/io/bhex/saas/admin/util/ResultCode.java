package io.bhex.saas.admin.util;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.util
 * @Author: ming.xu
 * @CreateDate: 12/08/2018 4:02 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public enum ResultCode {

    OK(0, "ok_template"),
    LOGIN_TOKEN_ERROR(5,"login token error"),
    FAIL(1, "fail_template"),
    ACCOUNT_NOT_FIND(10, "Account does not exist");


    private Integer code;

    private String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    //todo: messageResource
    public String getMsg() {
        return msg;
    }
}
