package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:17
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class BrokerTokenDTO implements Serializable {

    private Long exchangeId;
    private Long brokerId;
    //币种Id
    @NotNull
    private String tokenId;

    //币种名称
    //@NotNull
    private String tokenName;

    //币种全名
    @NotNull
    private String tokenFullName;

//    private String description;

    private String icon;

    //0沒有开给券商 1开给券商了
    private int status;

    private int brokerPublishStatus;
//    @NotNull
//    private Integer minPrecision;
//
//    private int isPublished;
//
//    //USDT 为虚拟币，USDTBTC不是虚拟币
//    private int isVirtual;
//
//    private BigDecimal withdrawMinQuantity;
//
//    private BigDecimal depositMinQuantity;
//
//    private int allowWithdraw;
//
//    private int allowDeposit;
//
//
//
    private String tokenDetail;


    //private Long applyExchangeId;
    private Long applyBrokerId;
    private Long createdAt;

    private Boolean isPrivateToken;

    private Boolean isBaas = false;
    private Boolean isAggregate = false;
    private Boolean isTest = false;



}
