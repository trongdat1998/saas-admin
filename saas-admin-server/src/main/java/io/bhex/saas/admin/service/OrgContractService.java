package io.bhex.saas.admin.service;

import io.bhex.base.admin.AddContractRequest;
import io.bhex.base.admin.ListContractReply;
import io.bhex.base.admin.OrgType;
import io.bhex.base.admin.UpdateContactInfoRequest;
import io.bhex.saas.admin.model.OrgContract;
import io.bhex.saas.admin.model.OrgContractDetail;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 3:43 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface OrgContractService {
    List<OrgContractDetail> getContractDetailList(List<Long> orgIdList);

    /**
     * 合作中的合同
     *
     * @param orgId
     * @param orgType
     * @param current
     * @param pageSize
     * @return
     */
    ListContractReply listOrgContract(Long orgId, OrgType orgType, Integer current, Integer pageSize);

    /**
     * 合作请求列表
     *
     * @param orgId
     * @param current
     * @param pageSize
     * @param orgType
     * @return
     */
    ListContractReply listApplication(Long orgId, OrgType orgType, Integer current, Integer pageSize);

    /**
     * 新建申请
     *
     * @param request
     * @return
     */
    Boolean addApplication(AddContractRequest request);

    /**
     * 编辑合作的备注信息
     *
     * @param request
     * @return
     */
    Boolean editContactInfo(UpdateContactInfoRequest request);

    /**
     * 同意合作
     *
     * @param orgId
     * @param cotractId
     * @param applyOrgType
     * @return
     */
    Boolean enableApplication(Long orgId, Long cotractId, OrgType applyOrgType);

    /**
     * 拒绝合作
     *
     * @param orgId
     * @param cotractId
     * @param applyOrgType
     * @return
     */
    Boolean rejectApplication(Long orgId, Long cotractId, OrgType applyOrgType);

    /**
     * 关闭合作
     *
     * @param orgId
     * @param cotractId
     * @param applyOrgType
     * @return
     */
    Boolean closeOrgContract(Long orgId, Long cotractId, OrgType applyOrgType);

    /**
     * 重新打开合作
     *
     * @param orgId
     * @param cotractId
     * @param applyOrgType
     * @return
     */
    Boolean reopenOrgContract(Long orgId, Long cotractId, OrgType applyOrgType);

    /**
     * 展示全部的合作机构。合作交易所的接口用到，展示合作的交易所名称
     *
     * @param orgIds
     * @param orgType
     * @return
     */
    ListContractReply listAllOrgContractInfo(List<Long> orgIds, OrgType orgType);

    /**
     * 券商和交易所处于合作中的OrgContract
     * @param exchangeId
     * @param brokerId
     * @return
     */
    OrgContract getOrgContractInContract(Long exchangeId, Long brokerId);


    Boolean editOrgContract(long exchangeId, long brokerId, long applyOrgId, boolean open, boolean trust);
}
