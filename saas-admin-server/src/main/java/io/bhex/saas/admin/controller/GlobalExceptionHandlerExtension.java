package io.bhex.saas.admin.controller;

import io.bhex.base.account.ConfigMatchTransferReply;
import io.bhex.base.token.PublishFuturesReply;
import io.bhex.bhop.common.controller.GlobalExceptionHandler;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.exception.PublishFuturesException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @ProjectName: exchange
 * @Package: io.bhex.ex.admin.controller
 * @Author: ming.xu
 * @CreateDate: 25/10/2019 3:57 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerExtension extends GlobalExceptionHandler {

    @ExceptionHandler(PublishFuturesException.class)
    @ResponseBody
    public ResultModel exception(PublishFuturesException e) {
        log.error("publish.futures.failed", e);
        PublishFuturesReply.PublishFuturesErrorCode replyCode = PublishFuturesReply.PublishFuturesErrorCode.forNumber(Integer.valueOf(e.getMessage()));
        String messageKey = "publish.futures.failed";
        switch (replyCode) {
            //成功设置
            case SUCCESS:
                messageKey = "request.success";
                break;
            case UNDERLYING_ERROR:
                messageKey = "publish.futures.underlying.error";
                break;
            case TOKEN_ERROR:
                messageKey = "publish.futures.token.error";
                break;
            case TOKEN_SETTING_ERROR:
                messageKey = "publish.futures.token.setting.error";
                break;
            case SYMBOL_ERROR:
                messageKey = "publish.futures.symbol.error";
                break;
            case SYMBOL_FUTURES_ERROR:
                messageKey = "publish.futures.symbol.futures.error";
                break;
            case RISK_LIMIT_ERROR:
                messageKey = "publish.futures.risk.limit.error";
                break;
            case EXCHANGE_SYMBOL_ERROR:
                messageKey = "publish.futures.exchange.symbol.error";
                break;
        }
        log.error("Publish Futures Exception: {}.", messageKey);
        return ResultModel.validateFail(getLocalMsg(messageKey));
    }
}
