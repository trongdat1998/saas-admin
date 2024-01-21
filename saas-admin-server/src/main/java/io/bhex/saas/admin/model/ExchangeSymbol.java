package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @Description:
 * @Date: 2018/11/5 上午10:14
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@Table(name = "tb_exchange_symbol")
public class ExchangeSymbol {

    @Id
    private Long id;

    private Long exchangeId;

    private String symbolId;

    private String symbolName;

    private String baseTokenId;

    private String quoteTokenId;

    private String symbolAlias;

    private Integer category;

    private String underlyingId;

    private Integer status;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
