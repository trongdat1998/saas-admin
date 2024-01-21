//package io.bhex.saas.admin.config;
//
//import io.bhex.bhop.common.config.LocaleMessageService;
//import io.bhex.bhop.common.exception.ErrorCode;
//import io.bhex.bhop.common.util.ResultModel;
//import io.bhex.saas.admin.controller.ExchangeSymbolController;
//import io.bhex.saas.admin.controller.ExchangeTokenController;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.annotation.Order;
//import org.springframework.validation.BindingResult;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestControllerAdvice(basePackageClasses = {ExchangeSymbolController.class, ExchangeTokenController.class})
//@Order(1)
//@Slf4j
//public class SaasAdminExceptionHandler {
//
//    @Autowired
//    private LocaleMessageService localeMessageService;
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseBody
//    public ResultModel methodArgumentNotValidException(MethodArgumentNotValidException ex) {
//        BindingResult result = ex.getBindingResult();
//        List<FieldError> fieldErrorList = result.getFieldErrors();
//        List<Map<String, String>> errors = new ArrayList<>();
//        List<String> messageStrings = new ArrayList<>();
//        //for(FieldError fieldError: fieldErrorList){ //只显示一条错误
//        FieldError fieldError = fieldErrorList.get(0);
//        Map<String, String> error = new HashMap<>();
//        log.info("{} {}", fieldError.getField(), fieldError.getDefaultMessage());
//        String errorMsg = fieldError.getDefaultMessage();
//        messageStrings.add(errorMsg);
//        error.put("field", fieldError.getField());
//        String errorMessage = localeMessageService.getMessage(errorMsg);
//        error.put("message", errorMessage);
//        errors.add(error);
//        //}
//
//        ResultModel resultModel = ResultModel.error(ErrorCode.ERR_REQUEST_PARAMETER.getCode(), String.join(";", ""));
//        resultModel.setData(errors);
//        return resultModel;
//    }
//}
//
//
