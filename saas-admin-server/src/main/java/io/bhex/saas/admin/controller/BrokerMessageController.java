package io.bhex.saas.admin.controller;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import io.bhex.base.admin.common.AdminUserReply;
import io.bhex.base.common.EditAppCertInfoRequest;
import io.bhex.base.common.MessageReply;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.controller.dto.AppPushSwitchDTO;
import io.bhex.saas.admin.controller.dto.DeliveryRecordDTO;
import io.bhex.saas.admin.controller.param.*;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.model.SmsSign;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.OrgInstanceService;
import io.bhex.saas.admin.service.SmsSignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Date: 2018/10/10 上午11:01
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/sms_sign")
public class BrokerMessageController {


    @Autowired
    private SmsSignService smsSignService;
    @Autowired
    private OrgInstanceService orgInstanceService;
    @Autowired
    private BrokerService brokerService;


    @RequestMapping(value = "/get_sms_sign")
    public ResultModel getSmsSign(@RequestBody @Valid BrokerIdPO po) {
        SmsSign smsSign = smsSignService.getByOrgId(po.getBrokerId());
        return ResultModel.ok(smsSign);
    }


    @RequestMapping(value = "/create")
    public ResultModel<Boolean> createSmsSign(@RequestBody @Valid BorkerSmsSignCreatePO po) {
        Broker broker = brokerService.getBrokerByBrokerName(po.getBrokerName());
        if (broker == null) {
            return ResultModel.error("brokerName.not.found");
        }
        BrokerInstanceDetail instanceDetail = orgInstanceService.getBrokerInstanceDetailByBrokerId(broker.getBrokerId());
        MessageReply r = smsSignService.createSmsSign(instanceDetail.getBrokerId(), instanceDetail.getBrokerName(), po.getSign());
        log.info("added;{}", r);
        if (r.getSuccess()) {
            return ResultModel.ok();
        } else {
            return ResultModel.error(r.getMessage());
        }
    }

    @RequestMapping(value = "/list")
    public ResultModel<List<SmsSign>> listSmsSigns(@RequestBody @Valid SmsSignListPO po) {
        List<SmsSign> signs = smsSignService.getSmsSigns(po.getFromId(), po.getLastId(), po.getPageSize());
        return ResultModel.ok(signs);
    }


    @RequestMapping(value = "/list_devlivery_records")
    public ResultModel<List<DeliveryRecordDTO>> listDeliveryRecords(@RequestBody @Valid ReceiverPO po) {
        List<DeliveryRecordDTO> records = smsSignService.getDeliveryRecords(po.getReceiver());
        return ResultModel.ok(records);
    }


    @RequestMapping(value = "/edit_app_cert_info", method = RequestMethod.POST)
    public ResultModel editAppCertInfo(@RequestBody @Valid EditAppCertPO certPO, AdminUserReply adminUser) {
        List<EditAppCertPO.AppCertPO> list = certPO.getItems();
        List<AppPushSwitchDTO> switches = smsSignService.getAppPushSwitches(certPO.getOrgId());
        if (CollectionUtils.isEmpty(switches)) {
            smsSignService.editPushSwitch(certPO.getOrgId(), "ALL_SITE", false, adminUser.getUsername());
        }
        for (EditAppCertPO.AppCertPO po : list) {
            EditAppCertInfoRequest.Builder builder = EditAppCertInfoRequest.newBuilder();

            builder.setOrgId(certPO.getOrgId()).setBundleId(po.getBundleId())
                    .setPushChannel(po.getPushChannel().toUpperCase())
                    .setDeveloperAppId(Strings.nullToEmpty(po.getDeveloperAppId()))
                    .setDeveloperSecretKey(po.getDeveloperSecretKey());

            if (po.getPushChannel().equalsIgnoreCase("apple")) {
                builder.setPassword(po.getPassword());
                builder.setAppChannel(po.getAppChannel());
            } else if (po.getPushChannel().equalsIgnoreCase("fcm")) {
                builder.setDeveloperAppId(po.getFcmDatabaseUrl());
            }
            smsSignService.editAppCertInfo(builder.build());
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/get_app_cert_info", method = RequestMethod.POST)
    public ResultModel getAppCertInfo(@RequestBody BrokerIdPO idPO) {
        return ResultModel.ok(smsSignService.getAppCertInfos(idPO.getBrokerId()));
    }

    @RequestMapping(value = "/edit_app_push_switch", method = RequestMethod.POST)
    public ResultModel editAppPushSwitch(@RequestBody @Valid EditAppPushSwitchPO po, AdminUserReply adminUser) {
        smsSignService.editPushSwitch(po.getOrgId(), po.getSwitchType(), po.getOpen(), adminUser.getUsername());
        return ResultModel.ok();
    }

    @RequestMapping(value = "/get_app_push_switches", method = RequestMethod.POST)
    public ResultModel getAppPushSwitches(@RequestBody AppPushSwitchListPO po) {
        List<AppPushSwitchDTO> list = smsSignService.getAppPushSwitches(po.getOrgId());
        return ResultModel.ok(list);
    }
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResultModel uploadCommonImage(@RequestParam(name = "uploadFile") MultipartFile uploadImageFile,
                                         @RequestParam(value = "echoStr", required = false, defaultValue = "") String echoStr,
                                         @RequestParam(value = "pushChannel", required = false, defaultValue = "") String pushChannel) throws Exception {
        Map<String, String> result = Maps.newHashMap();
        result.put("echoStr", echoStr);
        InputStream inputStream = uploadImageFile.getInputStream();

        if (pushChannel.equalsIgnoreCase("apple")) {
            String base64 = Base64.encodeBase64String(IOUtils.toByteArray(inputStream));
            result.put("secretKey", base64);
            return ResultModel.ok(result);
        } else if (pushChannel.equalsIgnoreCase("fcm")) {
            result.put("secretKey", IOUtils.toString(inputStream).replaceAll("\n", ""));
            return ResultModel.ok(result);
        }

        return ResultModel.ok();



    }
}
