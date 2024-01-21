package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@Table(name = "tb_trading_commission")
public class TradingCommission implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    /**
     * ID
     */
    private Long tradeDetailId;

    private Long exchangeId;

    private Long brokerId;

    private Long matchExchangeId;

    /**
     * account ID
     */
    private Long accountId;

    private String symbolId;

    private String feeTokenId;

    private Integer side;

    /**
     * 交易总额
     */
    private BigDecimal tradingAmount;

    private Timestamp matchTime;

    private Integer isMaker;

    private BigDecimal totalFee;

    private BigDecimal sysFee;

    private BigDecimal exchangeFee;

    private BigDecimal brokerFee;

    private BigDecimal exchangeSaasFee;

    private BigDecimal brokerSaasFee;

    private BigDecimal matchExchangeFee;

    private BigDecimal matchExchangeSaasFee;

    private BigDecimal sysFeeRate;

    private BigDecimal exchangeFeeRate;

    private BigDecimal exchangeSassFeeRate;

    private BigDecimal matchExchangeSaasFeeRate;

    private BigDecimal matchExchangeFeeRate;

    private BigDecimal brokerSassFeeRate;

    /**
     * calculate step status
     */
    private Integer status;

    /**
     * 转帐sn,取批次内最大的id
     */
    private Long sn;

    private String clearDay;

}
