package io.bhex.saas.admin.grpc.client;

import io.bhex.base.exadmin.SymbolFuturesRecordList;
import io.bhex.base.token.PublishFuturesReply;
import io.bhex.saas.admin.controller.param.AuditFuturesPO;
import io.bhex.saas.admin.model.ContractApplyRecord;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Date: 2019/10/10 下午17:11
 * @Author: xuming
 * @Copyright（C）: 201 BlueHelix Inc. All rights reserved.
 */
@Service
public interface ExchangeSwapClient {

    SymbolFuturesRecordList swapApplicationList(Integer current, Integer pageSize, Integer state);

   // int auditSwapRecord(AuditFuturesPO auditPO);

    PublishFuturesReply publishFutures(ContractApplyRecord symbolRecord);
}
