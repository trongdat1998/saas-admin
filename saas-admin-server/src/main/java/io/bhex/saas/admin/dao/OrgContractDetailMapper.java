package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.OrgContractDetail;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.dao
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 5:57 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface OrgContractDetailMapper extends Mapper<OrgContractDetail> {

    String TABLE_NAME = " tb_org_contract_detail ";

    String COLUMNS = " id, org_id, org_type, contract_id, contract_org_name, company_name, phone, email, contact, remark, created_at ";


}
