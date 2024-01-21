package io.bhex.saas.admin.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.model
 * @Author: ming.xu
 * @CreateDate: 09/08/2018 3:24 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_exchange_info")
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeInfo {

    @Id
    private Long id;

    private Long exchangeId;

    private String clusterName;

    private String exchangeName;

    //private BigDecimal saasFeeRate;

    private String company;

    private String email;

    private String contactName;

    private String contactTelephone;

    private Integer payEarnest;

    private String remark;

    private Integer status;

    private Long createdAt;

    private String createdIp;

    private Integer deleted;

    private Long updatedAt;

    @JsonIgnore
    private String saltStr;
}
