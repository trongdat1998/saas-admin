package io.bhex.saas.admin.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;


/**
 * @Description:
 * @Date: 2018/10/8 上午11:23
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_exchange_instance")
public class ExchangeInstance {
    @Id
    private Long id;

    private String clusterName;

    private String instanceName;

    private String gatewayUrl;


    private String adminInternalApiUrl;

    private String adminWebDomainPattern;

    private String adminWebProtocal;

    private int adminWebPort;

    private Timestamp createdAt;

    private String region;

    private String remark;

}
