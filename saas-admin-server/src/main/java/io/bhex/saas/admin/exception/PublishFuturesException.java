package io.bhex.saas.admin.exception;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.exception
 * @Author: ming.xu
 * @CreateDate: 2019/10/25 11:27 AM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public class PublishFuturesException extends RuntimeException {

    public PublishFuturesException() {
        super();
    }

    public PublishFuturesException(String message) {
        super(message);
    }

    public PublishFuturesException(String message, Throwable cause) {
        super(message, cause);
    }

    public PublishFuturesException(Throwable cause) {
        super(cause);
    }

    protected PublishFuturesException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
