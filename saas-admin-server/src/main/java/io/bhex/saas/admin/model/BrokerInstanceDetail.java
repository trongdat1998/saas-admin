package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_broker_instance_detail")
public class BrokerInstanceDetail {
    @Id
    private Long id;

    private Long instanceId;

    private Long brokerId;

    private String brokerName;


    private Timestamp createdAt;

    private Integer deleted;

    /** C端访问的domain设置*/
    private String brokerWebDomain;

    /** admin 自动生成的域名地址 */
    private String adminWebUrl;

    private String adminInternalApiUrl;

    private String adminWebDomain;

    /** 是否设置过dns 0-dns设置中 1-dns设置完成发送了邮件 2-邮件设置成功*/
    private Integer status;

    private Integer frontendCustomer; //券商前端自定义

    private Integer forbidAccess; //禁止访问

    private Long dueTime; //过期时间，0-代表未设置

}
