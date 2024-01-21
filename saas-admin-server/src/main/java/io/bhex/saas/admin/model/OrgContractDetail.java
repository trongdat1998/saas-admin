package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.model
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 4:10 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Table(name = "tb_org_contract_detail")
public class OrgContractDetail {

    @Id
    private Long id;
    private Long orgId;
    private Integer orgType;
    private Long contractId;
    private String contractOrgName; //合作的机构名称
    // 以下为合作机构的联系信息
    private String companyName;
    private String phone;
    private String email;
    private String contact;
    private String remark;
    private Long createdAt;

    public OrgContractDetail() {
        this.companyName = "";
        this.phone = "";
        this.email = "";
        this.contact = "";
        this.remark = "";
    }
}
