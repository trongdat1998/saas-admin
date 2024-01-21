package io.bhex.saas.admin.service;

import io.bhex.base.bhadmin.ContractApplyList;
import io.bhex.base.bhadmin.GetSymbolPager;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.saas.admin.controller.dto.SymbolFuturesRecordDTO;
import io.bhex.saas.admin.controller.param.AuditFuturesPO;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service
 * @Author: ming.xu
 * @CreateDate: 2019/10/10 5:21 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface ExchangeSwapService {

    PaginationVO<SymbolFuturesRecordDTO> applicationList(Integer current, Integer pageSize, Integer state);

    int auditSymbolRecord(AuditFuturesPO auditPO);

    void auditSymbolFull(long applyId, long eventId);

    ContractApplyList listContractApply(GetSymbolPager request);
}
