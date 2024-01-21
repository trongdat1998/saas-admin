package io.bhex.saas.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import io.bhex.base.admin.AddVerifyRecordRequest;
import io.bhex.base.admin.VerifyFlowError;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.service.AdminLoginUserService;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.RequestUtil;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.*;
import io.bhex.saas.admin.controller.param.VerifyPO;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.model.ExchangeInfo;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.impl.VerifyFlowService;
import io.bhex.saas.admin.util.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/8/19 上午10:30
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping
public class ExchangeController extends BaseController {

    private static final Integer CATEGORY_COIN = 1;
    @Autowired
    private AdminLoginUserService adminLoginUserService;
    @Autowired
    private ExchangeInfoService exchangeInfoService;
    @Autowired
    private VerifyFlowService verifyFlowService;

    @AccessAnnotation(verifyAuth = false)
    @RequestMapping(value = "/api/v1/exchange/query_simple_exchanges", method = RequestMethod.POST)
    public ResultModel listSimpleExchanges() {
        List<ExchangeInfo> exchangeInfos = exchangeInfoService.queryAllExchanges();
        List<Map<String, Object>> result = exchangeInfos.stream().map(b -> {
            Map<String, Object> item = new HashMap<>();
            item.put("exchangeId", b.getExchangeId());
            item.put("name", b.getExchangeName());
            return item;
        }).collect(Collectors.toList());
        return ResultModel.ok(result);
    }

    @RequestMapping(value = "/api/v1/exchange/query_exchanges", method = RequestMethod.POST)
    public ResultModel<PaginationVO<ExchangeInfoRes>> queryExchanges(@RequestBody @Valid QueryExchangesPO po) {
        int current = po.getCurrent();
        current = current <= 1 ? 1 : current;
        Combo2<List<ExchangeInfo>, Integer> result = exchangeInfoService
                .queryExchangeInfos(current, po.getPageSize(), po.getExchangeName(), po.getExchangeId());
        PaginationVO<ExchangeInfoRes> vo = new PaginationVO();
        vo.setCurrent(current);
        vo.setPageSize(po.getPageSize());
        vo.setTotal(result.getV2());

        if (result.getV2() == 0) {
            return ResultModel.ok(vo);
        }

        List<VerifyFlowRecordDTO> verifyFlowRecords = verifyFlowService.queryAllRecords(0, BizConstant.CHANGE_EXCHANGE_BIZ_TYPE,
                Lists.newArrayList(VerifyBizRecord.VERIFING_STATUS), 0, 100, 0L);
        if (CollectionUtils.isEmpty(verifyFlowRecords)) {
            verifyFlowRecords = Lists.newArrayList();
        }
        List<Long> verifyingList = verifyFlowRecords.stream().map(v -> {
            EditExchangePO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(v.getVerifyContent()), EditExchangePO.class);
            return verifyPO.getId();
        }).collect(Collectors.toList());

        List<ExchangeInfoRes> resList = new ArrayList<>();
        for (ExchangeInfo info : result.getV1()) {
            ExchangeInfoRes res = new ExchangeInfoRes();
            BeanUtils.copyProperties(info, res);

            ExchangeInstanceDetail instance = exchangeInfoService.getInstanceInfoByExchangeId(info.getExchangeId());
            res.setDnsSetting(instance.getStatus());
            res.setAdminWebUrl(instance.getAdminWebUrl());
            res.setForbidAccessStatus(instance.getForbidAccess() == 1);
            res.setSaasFee(exchangeInfoService.getSaasFee(info.getExchangeId(), CATEGORY_COIN));
            res.setVerifying(verifyingList.contains(info.getId()));
            resList.add(res);
        }
        vo.setList(resList);
        return ResultModel.ok(vo);
    }

    @AccessAnnotation(authIds = {7L, 1402L})
    @RequestMapping(value = "/api/v1/exchange/query_exchange_detail", method = RequestMethod.POST)
    public ResultModel<ExchangeInfoRes> queryExchangeDetail(@RequestBody @Valid ExchangeIdPO po) {

        ExchangeInfo info = exchangeInfoService.getExchangeInfoById(po.getExchangeId());
        ExchangeInfoRes res = new ExchangeInfoRes();
        BeanUtils.copyProperties(info, res);
        ExchangeInstanceDetail instance = exchangeInfoService.getInstanceInfoByExchangeId(info.getExchangeId());
        res.setDnsSetting(instance.getStatus());
        res.setAdminWebUrl(instance.getAdminWebUrl());
        res.setForbidAccessStatus(instance.getForbidAccess() == 1);
        res.setSaasFee(exchangeInfoService.getSaasFee(info.getExchangeId(), CATEGORY_COIN));
        return ResultModel.ok(res);
    }

    @RequestMapping(value = "/api/v1/exchange/create_exchange", method = RequestMethod.POST)
    public ResultModel createExchange(HttpServletRequest request, @RequestBody @Validated CreateExchangePO exchangePO) {
        String exchangeName = exchangePO.getExchangeName();
        ExchangeInfo info = exchangeInfoService.getExchangeInfoByExchangeName(exchangeName);
        if (info != null) {
            log.warn("{} existed", exchangeName);
            return ResultModel.withArgsNoData(ErrorCode.REQUEST_PARAMETER_VALIDATE_FAIL.getCode(), "has.existed", "field.exchangeName");
        }


        ExchangeInfo exchangeInfo = new ExchangeInfo();
        BeanUtils.copyProperties(exchangePO, exchangeInfo);
        exchangeInfo.setCreatedAt(System.currentTimeMillis());
        exchangeInfo.setUpdatedAt(System.currentTimeMillis());
        exchangeInfo.setCreatedIp(RequestUtil.getRealIP(request));
        exchangeInfo.setDeleted(0);
        exchangeInfo.setStatus(0);
        exchangeInfo.setExchangeId(0L);
        exchangeInfo.setPayEarnest(0);
        Combo2<Boolean, String> result = exchangeInfoService.addExchangeInfo(1L, exchangeInfo, exchangePO.getSaasFee(), CATEGORY_COIN);
        if (result.getV1()) {
            return ResultModel.ok();
        }
        return ResultModel.error(result.getV2());
    }



    @RequestMapping(value = "/api/v1/exchange/{id}", method = RequestMethod.GET)
    public ResultModel<ExchangeInfoRes> queryExchangeDetail(@PathVariable(value = "id") Long id) {
        ExchangeInfo info = exchangeInfoService.getExchangeInfoById(id);
        ExchangeInfoRes res = new ExchangeInfoRes();
        BeanUtils.copyProperties(info, res);
        res.setSaasFee(exchangeInfoService.getSaasFee(info.getExchangeId(), CATEGORY_COIN));
        return ResultModel.ok(res);
    }

    @RequestMapping(value = "/api/v1/exchange/check_exchange_name", method = RequestMethod.POST)
    public ResultModel<Boolean> checkExchangeName(@RequestParam String exchangeName) {
        ExchangeInfo info = exchangeInfoService.getExchangeInfoByExchangeName(exchangeName);
        boolean existed = info != null;
        return ResultModel.ok(existed);
    }

    @RequestMapping(value = "/api/v1/exchange/enable_exchange", method = RequestMethod.POST)
    public ResultModel enableExchange(@RequestBody EnableExchangePO po) {
        ExchangeInfo info = exchangeInfoService.getExchangeInfoById(po.getId());
        if (info == null) {
            return ResultModel.validateFail("not.found", "field.exchangeInfo");
        }
        if (info.getStatus() == 1) {
            log.warn("id:{} exchange info status is enable!", po.getId());
            return ResultModel.ok("exchange info status is enable!");
        }
        boolean suc = exchangeInfoService.updateExchangeStatus(po.getId(), 1);
        return suc ? ResultModel.ok("enable successfully")  : ResultModel.error("enable failed");
    }

    @RequestMapping(value = "/api/v1/exchange/disable_exchange", method = RequestMethod.POST)
    public ResultModel disableExchange(@RequestBody DisableExchangePO po) {
        ExchangeInfo info = exchangeInfoService.getExchangeInfoById(po.getId());
        if (info == null) {
            return ResultModel.validateFail("not.found", "field.exchangeInfo");
        }
        if (info.getStatus() == 0) {
            log.warn("id:{} exchange info status is disable!", po.getId());
            return ResultModel.ok("exchange info status is disable!");
        }
        boolean suc = exchangeInfoService.updateExchangeStatus(po.getId(), 0);
        return suc ? ResultModel.ok("disable successfully") : ResultModel.error("disable failed");
    }

    @RequestMapping(value = "/api/v1/exchange/forbid_access", method = RequestMethod.POST)
    public ResultModel forbidExchangeAccess(@RequestBody @Validated IdPO po) {
        boolean isOk = exchangeInfoService.forbidAccess(po.getId());
        if (isOk) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMsg());
        }
    }


    @RequestMapping(value = "/api/v1/exchange/send_set_password_email", method = RequestMethod.POST)
    public ResultModel sendSetPasswordEmail(@RequestBody @Validated ExchangeIdPO po) {
        ExchangeInstanceDetail instance = exchangeInfoService.getInstanceInfoByExchangeId(po.getExchangeId());
        if (instance.getStatus() == 2) { //已发送过邮件并设置成功
            return ResultModel.ok();
        }
        exchangeInfoService.updateExchangeInstanceStatus(po.getExchangeId(), 1);

        exchangeInfoService.sendSetPasswordEmail(po.getExchangeId());
        return ResultModel.ok();
    }

    @RequestMapping(value = "/api/v1/exchange/add_exchange_change_verify", method = RequestMethod.POST)
    public ResultModel addExchangeChangeVerify(@RequestBody @Validated EditExchangePO po, AdminUserReply adminUser) {
        AddVerifyRecordRequest request = AddVerifyRecordRequest.newBuilder()
                .setOrgId(adminUser.getOrgId())
                .setAdminUserId(adminUser.getId())
                .setAdminUserName(adminUser.getUsername())
                .setBizType(BizConstant.CHANGE_EXCHANGE_BIZ_TYPE)
                .setTitle("交易所修改")
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
            return verifyExchangeChangeNoVerifyAdvance(verifyPO, adminUser);
        }
        return ResultModel.ok();
    }

    private ResultModel verifyExchangeChangeNoVerifyAdvance(VerifyPO po, AdminUserReply adminUser) {
        VerifyFlowRecordDTO recordDTO = verifyFlowService.queryRecord(po.getId());
        if (recordDTO.getBizType() != BizConstant.CHANGE_EXCHANGE_BIZ_TYPE) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
        }
        Pair<VerifyFlowError, Integer> pair = verifyFlowService.addVerifyResult(po.getId(), adminUser.getId(),
                adminUser.getUsername(), po.getPassed(), po.getReviewComments());

        if (pair.getLeft() == VerifyFlowError.OK && pair.getRight() == VerifyBizRecord.PASSED_STATUS) {
            EditExchangePO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(recordDTO.getVerifyContent()), EditExchangePO.class);
            boolean result = exchangeInfoService.editExchangeInfo(verifyPO,  CATEGORY_COIN);
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

    @RequestMapping(value = "/api/v1/verify_flow/verify_exchange_change", method = RequestMethod.POST)
    public ResultModel verifyExchangeChange(@RequestBody @Validated VerifyPO po, AdminUserReply adminUser) {
        adminLoginUserService.verifyAdvance(po.getAuthType(), po.getVerifyCode(), adminUser.getId(), adminUser.getOrgId(), getAdminPlatform());
        return verifyExchangeChangeNoVerifyAdvance(po, adminUser);
    }


}
