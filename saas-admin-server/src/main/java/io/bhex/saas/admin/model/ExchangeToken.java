package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
@Table(name = "tb_exchange_token")
public class ExchangeToken {
    @Id
    private Long id;

    private Long exchangeId;

    private String tokenId;
    //币种全名
    private String tokenFullName;

    private String description;

    private Integer minPrecision;

    //USDT 为虚拟币，USDTBTC不是虚拟币
//    private Integer isVirtual;
//
//
//    private Integer allowWithdraw;
//
//    private Integer allowDeposit;

    private String icon;

    private String tokenDetail;

//    private Integer isPublished;

    private String feeTokenId;
    private String feeTokenName;

    //0不显示 1显示
    private Integer status;

    private Integer category;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
