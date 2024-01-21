package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.model
 * @Author: ming.xu
 * @CreateDate: 09/08/2018 3:27 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_broker_instance")
public class BrokerInstance {

    @Id
    private Long id;

    private String instanceName;

    private String adminInternalApiUrl;

    private String adminWebDomainPattern;

    private String brokerWebDomainPattern;


    private String adminWebProtocal;

    private int adminWebPort;

    private String region;

    private String remark;

    private Integer status;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}
