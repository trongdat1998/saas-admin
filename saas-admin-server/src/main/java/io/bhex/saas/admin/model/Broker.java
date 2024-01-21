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
 * @CreateDate: 09/08/2018 12:10 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Builder
@Data
@Table(name = "tb_broker")
@AllArgsConstructor
@NoArgsConstructor
public class Broker {

    @Id
    private Long id;
    private Long brokerId;
    private Long instanceId;
    private String name;
    private String company;
    private String email;
    private String phone;
    private String host;
    private String earnestAddress;
    private String contact;
    private String basicInfo;
    private String remark;
    private String apiDomain;
    //private BigDecimal saasFeeRate;
    private Boolean isBind;
    private Boolean enabled;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Integer registerOption;
    @JsonIgnore
    private String saltStr;
}
