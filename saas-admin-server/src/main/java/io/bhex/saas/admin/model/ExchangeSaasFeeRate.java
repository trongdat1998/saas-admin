package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.Date;
import java.sql.Timestamp;


/**
 * @Description: 交易所saas费率设置
 * @Date: 2018/9/3 下午5:30
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "tb_exchange_saas_fee_rate")
public class ExchangeSaasFeeRate {

    @Id
    private Long id;

    private Long exchangeId;

    /**
     * 收取的Saas费率
     */
    private BigDecimal feeRate;

    /**
     * 生效时间
     */
    private Date actionTime;

    private Timestamp createAt;

    private Timestamp updateAt;

    /**
     * 0-未删除 1-已删除
     */
    private int deleted;
}
