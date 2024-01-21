package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

/**
 * @Description:
 * @Date: 2018/8/17 上午11:24
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_exchange_instance_detail")
public class ExchangeInstanceDetail {

    @Id
    private Long id;

    private Long instanceId;

    private Long exchangeId;

    private String exchangeName;

    private String clusterName;

    private String instanceName;

    private String gatewayUrl;


    private Timestamp createdAt;

    private Integer deleted;

    /** admin 自动生成的域名地址 */
    private String adminWebUrl;

    private String adminInternalApiUrl;

    private String adminWebDomain;

    /** 是否设置过dns 0-dns设置中 1-dns设置完成发送了邮件 2-邮件设置成功*/
    private Integer status;

    private Integer forbidAccess; //禁止访问

}
