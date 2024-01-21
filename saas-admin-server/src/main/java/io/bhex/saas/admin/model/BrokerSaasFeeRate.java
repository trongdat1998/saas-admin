package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @Description:券商saas费率表
 * @Date: 2018/9/3 下午5:33
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_broker_saas_fee_rate")
public class BrokerSaasFeeRate {

    @Id
    private Long id;

    private Long brokerId;

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
