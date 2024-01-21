package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.model
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 3:32 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_org_contract")
public class OrgContract {

    public final static int APPLY_STATUS = 0;
    public final static int ALLOW_STATUS = 1;
    public final static int REJECTED_STATUS = 2;
    public final static int CLOSED_STATUS = 3;
    public final static int REOPEN_STATUS = 4;
    public final static int OTHER_CONFIRMED_STATUS = 5;

    public final static Integer BROKER_ORG = 1;
    public final static Integer EXCHANGE_RG = 2;

    @Id
    private Long id;
    private Long brokerId;
    private Long contractId;
    private Long exchangeId;
    //合作状态变更的发起机构id，用来判断下次状态变更时是否有权限操作
    private Long applyOrgId;
    private Integer brokerStatus;
    private Integer exchangeStatus;
    private Long createdAt;
}
