package io.bhex.saas.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.exception.BrokerErrorCode;
import io.bhex.broker.common.exception.BrokerException;
import io.bhex.broker.grpc.admin.FetchOneResponse;
import io.bhex.broker.grpc.common_ini.CommonIni;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.*;
import io.bhex.saas.admin.controller.param.FetchOnePO;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.CommonIniService;
import io.bhex.saas.admin.service.DBUtilsService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping
public class OperateController extends BaseController {

    @Autowired
    private CommonIniService commonIniService;

    @Autowired
    private AdminLoginUserService adminLoginUserService;

    @Autowired
    private VerifyFlowService verifyFlowService;

    @Autowired
    private BrokerService brokerService;

    @Autowired
    private DBUtilsService dbUtilsService;


    @RequestMapping(value = "/api/v1/operate/query_transfer_whitelist", method = RequestMethod.POST)
    public ResultModel addTransferSymbolWhiteList(@RequestBody @Validated QueryBrokerTransferWhitelistPO po, AdminUserReply adminUser) {
        PaginationVO<BrokerInfoRes> vo = brokerService.queryBrokerTransferWhitelist(po.getCurrent(), po.getPageSize(), po.getBrokerName(), po.getBrokerId());
        return ResultModel.ok(vo);
    }

    @RequestMapping(value = "/api/v1/operate/get_transfer_whitelist", method = RequestMethod.POST)
    public ResultModel getTransferWhiteList(@RequestBody @Validated GetTransferWhitelistPO po, AdminUserReply adminUser) {
        CommonIni commonIni = commonIniService.getCommonIni(po.getBrokerId(), "transferTokens", "");
        return ResultModel.ok(commonIni == null || !commonIni.isInitialized() ? null : GetTransferWhitelistRes.builder()
                .iniDesc(commonIni.getIniDesc())
                .iniName(commonIni.getIniName())
                .iniValue(commonIni.getIniValue())
                .build());
    }

    /**
     * 增加转账币对白名单配置申请
     *
     * @param po
     * @param adminUser
     * @return
     */
    @RequestMapping(value = "/api/v1/operate/set_transfer_whitelist_verify", method = RequestMethod.POST)
    public ResultModel addTransferWhiteList(@RequestBody @Validated SetTransferWhitelistPO po, AdminUserReply adminUser) {
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.TRANSFER_WHITELIST_BIZ_TYPE)
                .setTitle("转账币对白名单配置")
                .setDescription("")
                .setStatus(VerifyBizRecord.VERIFING_STATUS)
                .setVerifyContent(JSON.toJSONString(po))
                .build();
        VerifyBizRecord bizRecord = verifyFlowService.addVerifyBizRecord(request);
        if (bizRecord.getCurrentVerifyLevel() == BizConstant.VERIFY_FINAL_LEVEL) { //无需审核直接走到头
            VerifyPO verifyPO = new VerifyPO();
            verifyPO.setId(bizRecord.getId());
            verifyPO.setPassed(true);
            verifyPO.setReviewComments("");
            return verifySymbolWhiteListNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }

    private ResultModel verifySymbolWhiteListNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.TRANSFER_WHITELIST_BIZ_TYPE) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            SetTransferWhitelistPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), SetTransferWhitelistPO.class);
            CommonIni commonIni = commonIniService.getCommonIni(verifyPO.getBrokerId(), "transferTokens", "");
            log.info("verify_transfer_symbol_whitelist from: {}", commonIni == null ? "":commonIni.getIniValue());
            boolean result = commonIniService.saveCommonIni(verifyPO.getBrokerId(), "transferTokens", "", verifyPO.getContent(), "");
            if (result) {
                return ResultModel.ok();
            }
            return ResultModel.error("edit failed");
        }

        if (pair.getLeft() != VerifyFlowError.OK) {
            return ResultModel.error(pair.getLeft().name());
        }
        return ResultModel.ok();
    }

    /**
     * 审核转账币对白名单
     *
     * @param po
     * @param adminUser
     * @return
     */
    @RequestMapping(value = "/api/v1/broker/verify_transfer_symbol_whitelist", method = RequestMethod.POST)
    public ResultModel verifySymbolWhiteList(@RequestBody @Validated VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return verifySymbolWhiteListNoVerifyAdvance(po, adminUser);
    }

    /**
     * 查询单条，只允许and拼接
     *
     * @param po
     * @return
     */
    @RequestMapping(value = "/api/v1/dbutils/fetch_one", method = RequestMethod.POST)
    public ResultModel fetchOne(@RequestBody @Validated FetchOnePO po) {
        List<String[]> conditions = new LinkedList<>();
        for (String condition : po.getConditions()) {
            String[] arr = splitByCondition(condition);
            if (arr == null) {
                throw new BrokerException(BrokerErrorCode.PARAM_ERROR);
            }
            conditions.add(arr);
        }
        FetchOneResponse response = dbUtilsService.fetchOne(po.getNamespace(), po.getDbName(), po.getTableName(), po.getFields().split(","), conditions);
        if (response.getRet() == 0) {
            return ResultModel.ok(JSONObject.parse(response.getResult()));
        } else {
            return ResultModel.error(response.getResult());
        }
    }

    /**
     * 查询所有表配置
     *
     * @return
     */
    @RequestMapping(value = "/api/v1/dbutils/tables", method = RequestMethod.GET)
    public ResultModel getTables() {
        return ResultModel.ok(dbUtilsService.getTableConfigs());
    }

    private static String[] splitByCondition(String condition) {
        int len = condition.length();
        String left = null, cond = null, right = null;
        try {
            for (int i = 0; i < len; i++) {
                char c = condition.charAt(i);
                if (c == '>') {
                    left = condition.substring(0, i);
                    char next = condition.charAt(i + 1);
                    if (next == '=') {
                        cond = ">=";
                        right = condition.substring(i + 2);
                    } else {
                        cond = ">";
                        right = condition.substring(i + 1);
                    }
                    break;
                } else if (c == '<') {
                    left = condition.substring(0, i);
                    char next = condition.charAt(i + 1);
                    if (next == '=' || next == '>') {
                        cond = new StringBuffer().append(c).append(next).toString();
                        right = condition.substring(i + 2);
                    } else {
                        cond = "<";
                        right = condition.substring(i + 1);
                    }
                    break;
                } else if (c == '!') {
                    left = condition.substring(0, i);
                    char next = condition.charAt(i + 1);
                    if (next == '=') {
                        cond = new StringBuffer().append(c).append(next).toString();
                        right = condition.substring(i + 2);
                    } else {
                        return null;
                    }
                    break;
                } else if (c == '=') {
                    left = condition.substring(0, i);
                    cond = "=";
                    right = condition.substring(i + 1);
                    break;
                }
            }
            if (left == null || cond == null || right == null) {
                return null;
            }
            left = StringUtils.strip(left, " '\"");
            right = StringUtils.strip(right, " '\"");
            return new String[]{left, cond, right};
        } catch (Exception e) {
            return null;
        }
    }
}
