package io.bhex.saas.admin.controller;

import io.bhex.base.margin.cross.UpdateFundingCrossReply;
import io.bhex.base.proto.ErrorCode;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.MarginFundingCrossDTO;
import io.bhex.saas.admin.controller.param.BindMarginFundingPO;
import io.bhex.saas.admin.controller.param.QueryMarginFundingAccountPO;
import io.bhex.saas.admin.service.MarginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 15:38
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/margin")
public class MarginController {

    @Resource
    MarginService marginService;

    @RequestMapping(value = "/bind_funding_account", method = RequestMethod.POST)
    public ResultModel bindFundingAccount(@RequestBody @Validated BindMarginFundingPO po) {
        UpdateFundingCrossReply reply = marginService.updateFundingCross(po.getOrgId(), po.getAccountId());
        if (reply.getCode() != ErrorCode.SUCCESS) {
            ResultModel.error("bind margin funding error");
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/query_funding_account")
    public ResultModel queryFundingAccount(@RequestBody @Validated QueryMarginFundingAccountPO po) {
        List<MarginFundingCrossDTO> result = marginService.queryFundingCross(po.getOrgId());
        return ResultModel.ok(result);
    }
}
