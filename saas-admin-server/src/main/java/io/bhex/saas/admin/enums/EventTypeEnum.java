package io.bhex.saas.admin.enums;

public enum EventTypeEnum {
    AUDIT_SYMBOL_SUCCESS,
    AUDIT_CONTRACT_SUCCESS,
    CLOSE_SYMBOL_TANSFER_TO_ME ,
    PUBLISH_BROKER_SYMBOL,
    CLOSE_BROKER_SYMBOL, //下架币对
    CANCEL_LETF_ORDERS, //撤销etf杠杆全部订单
    CANCEL_ALL_ORDERS, //撤销币对全部订单
    ;
}
