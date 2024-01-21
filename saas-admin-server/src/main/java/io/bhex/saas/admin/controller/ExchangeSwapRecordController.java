package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.constants.ApplyStateEnum;
import io.bhex.saas.admin.controller.dto.SymbolFuturesRecordDTO;
import io.bhex.saas.admin.controller.param.ApplicationListPO;
import io.bhex.saas.admin.controller.param.AuditFuturesPO;
import io.bhex.saas.admin.service.ExchangeSwapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.controller
 * @Author: ming.xu
 * @CreateDate: 2019/10/10 5:15 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/exchange_swap")
public class ExchangeSwapRecordController {

    @Autowired
    private ExchangeSwapService exchangeSwapService;

    @PostMapping("/application/audit")
    public ResultModel auditSymbolApplication(@RequestBody @Validated AuditFuturesPO auditPO) {
        ApplyStateEnum curStateEnum = ApplyStateEnum.getByState(auditPO.getCurState());
        ApplyStateEnum toStateEnum = ApplyStateEnum.getByState(auditPO.getToState());
        if (curStateEnum.equals(toStateEnum)) {
            return ResultModel.ok();
        }
        exchangeSwapService.auditSymbolRecord(auditPO);
        return ResultModel.ok();
    }

    @PostMapping("/application/list")
    public ResultModel<PaginationVO<SymbolFuturesRecordDTO>> queryApplicationList(@RequestBody @Validated ApplicationListPO param) {
        ApplyStateEnum stateEnum = ApplyStateEnum.getByState(param.getState());
        Integer current = Objects.isNull(param.getCurrent())? 1: param.getCurrent();
        Integer pageSize = Objects.isNull(param.getPageSize())? 30: param.getPageSize();
        return ResultModel.ok(exchangeSwapService.applicationList(current, pageSize, stateEnum.getState()));
    }
}
