package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description:
 * @Date: 2018/8/9 下午4:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class BrokerSymbolDTO implements Serializable {

    @NotNull
    private String symbolId;
    @NotNull
    private String symbolName;
    @NotNull
    private String baseTokenId;

    @NotNull
    private String quoteTokenId;
    private String symbolAlias;
    private BigDecimal minTradeQuantity;
    private BigDecimal minTradeAmount;
    private BigDecimal minPricePrecision;
    private BigDecimal basePrecision;
    private BigDecimal quotePrecision;
    private Boolean allowTrade;
    private Boolean saasAllowTradeStatus;
    private Boolean published;

    private Long exchangeId;
    private Boolean showStatus; //券商是否上架状态

    private Long applyExchangeId;
    private Long applyBrokerId;

    private Long createdAt;

    private Boolean isPrivateSymbol = false;

    private Boolean isBaas = false;
    private Boolean isAggregate = false;
    private Boolean isTest = false;
    private Boolean isMainstream = false;

    private Integer updateStatus;

}
