package io.bhex.saas.admin.controller;

import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.entity.AdminUser;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.controller.param.VerifyFlowConfigPO;
import io.bhex.saas.admin.controller.param.VerifyListPO;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.model.VerifyFlowConfig;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/api/v1/verify_flow")
@RestController
@Slf4j
public class VerifyFlowConfigController {

    @Autowired
    private VerifyFlowService verifyFlowService;

    @RequestMapping("/verify_users")
    public ResultModel queryVerifyAdminUsers(AdminUserReply adminUser) {
        List<AdminUser> users = verifyFlowService.queryVerifyAdminUsers(adminUser.getOrgId());
        List<Map<String, Object>> result = new ArrayList<>();
        users.forEach(u -> {
            Map<String, Object> user = new HashMap<>();
            user.put("adminUserId", u.getId() + "");
            user.put("adminUserName", u.getUsername() + "");
            result.add(user);
        });
        return ResultModel.ok(result);
    }

    @RequestMapping("/configs")
    public ResultModel queryVerifyFlowConfigs(AdminUserReply adminUser) {
        List<VerifyFlowConfig> configs = verifyFlowService.queryVerifyFlowConfigs(adminUser.getOrgId());
        List<Map<String, Object>> result = new ArrayList<>();
        configs.forEach(u -> {
            Map<String, Object> config = new HashMap<>();
            config.put("id", u.getId());
            config.put("created", u.getCreatedAt());
            config.put("adminUserName", u.getAdminUserName());
            config.put("level", u.getLevel());
            config.put("bizType", u.getBizType());
            config.put("canChange", u.getCanChange() == 1);
            config.put("canClose", u.getCanClose() == 1);
            config.put("verifyUserIds", u.getVerifyUserIds().split(","));
            config.put("status", u.getStatus());
            result.add(config);
        });
        return ResultModel.ok(result);
    }

    @RequestMapping("/edit_verify_config")
    public ResultModel editVerifyFlowConfig(AdminUserReply adminUser, @RequestBody @Valid VerifyFlowConfigPO po) {
        verifyFlowService.editVerifyConfig(adminUser.getOrgId(), po.getBizType(), po.getCanChange(),
                po.getCanClose(), po.getVerifyUserIds(), adminUser.getId(), adminUser.getUsername());

        return ResultModel.ok();
    }

    @RequestMapping("/close_config")
    public ResultModel closeVerifyFlowConfig(AdminUserReply adminUser, @RequestBody @Valid IdPO po) {
        verifyFlowService.closeVerifyConfig(adminUser.getOrgId(), po.getId());
        return ResultModel.ok();
    }

    @RequestMapping("/record_list")
    public ResultModel queryVerifyList(@RequestBody @Valid VerifyListPO po, AdminUserReply adminUser) {

        List<VerifyFlowRecordDTO> list = verifyFlowService.queryVerifiedRecords(0,
                po.getBizType(), po.getLastId(), po.getPageSize(), adminUser.getId());
        list.forEach(d -> d.setStatus(d.getStatus() > 10 ? d.getStatus() / 10 : d.getStatus()));
        return ResultModel.ok(list);
    }


    @RequestMapping("/detail")
    public ResultModel querySaasTransferDetail(@RequestBody @Valid IdPO po) {
        VerifyFlowRecordDTO dto = verifyFlowService.queryRecord(po.getId());

        if (dto.getStatus() > 10) {
            dto.setStatus(dto.getStatus() / 10);
        }
        if (dto.getBizType() == BizConstant.CHANGE_EXCHANGE_BIZ_TYPE) {
            BigDecimal saasFee = new BigDecimal(dto.getVerifyContent().getOrDefault("saasFee", "0").toString());
            if (saasFee.compareTo(BigDecimal.ZERO) > 0) {
                dto.getVerifyContent().put("saasFee", saasFee.multiply(new BigDecimal(100)));
            }
        }

        return ResultModel.ok(dto);
    }

}
