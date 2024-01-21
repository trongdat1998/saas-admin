package io.bhex.saas.admin.service;


import com.google.common.collect.Lists;
import io.bhex.base.common.*;
import io.bhex.saas.admin.controller.dto.AppPushSwitchDTO;
import io.bhex.saas.admin.controller.dto.DeliveryRecordDTO;
import io.bhex.saas.admin.controller.param.EditAppCertPO;
import io.bhex.saas.admin.dao.BrokerMapper;
import io.bhex.saas.admin.grpc.client.impl.SmsSignClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.SmsSign;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/10/10 上午10:54
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Service
public class SmsSignService {

    @Autowired
    private SmsSignClient smsSignClient;
    @Autowired
    private BrokerMapper brokerMapper;

    public MessageReply createSmsSign(Long orgId, String brokerName, String sign) {
        return smsSignClient.createSmsSign(orgId, brokerName, sign);
    }

    public SmsSign getByOrgId(Long orgId) {
        return smsSignClient.getSmsSign(orgId);
    }

    public List<SmsSign> getSmsSigns(Long fromId, Long endId, Integer limit) {
        return smsSignClient.getSmsSigns(0L, fromId, endId, limit);
    }

    public List<SmsSign> selectSmsSignsByLastModify(List<Long> brokerIds, Long lastModify) {
        return new ArrayList<>();
    }

    public List<DeliveryRecordDTO> getDeliveryRecords(String receiver) {
        List<DeliveryRecord> records = smsSignClient.getDeliveryRecords(receiver);
        if (CollectionUtils.isEmpty(records)) {
            return new ArrayList<>();
        }
        List<DeliveryRecordDTO> result = records.stream().map(r -> {
            DeliveryRecordDTO dto = new DeliveryRecordDTO();
            BeanUtils.copyProperties(r, dto);
            return dto;
        }).collect(Collectors.toList());
        return result;
    }

    public MessageReply editAppCertInfo(EditAppCertInfoRequest request) {
        return smsSignClient.editAppCertInfo(request);
    }

    public EditAppCertPO getAppCertInfos(long orgId) {
        List<GetAppCertInfosReply.AppCertInfo> list = smsSignClient.getAppCertInfos(GetAppCertInfosRequest.newBuilder().setOrgId(orgId).build());
        EditAppCertPO po = new EditAppCertPO();
        if (CollectionUtils.isEmpty(list)) {
            po.setOrgId(orgId);
            po.setItems(Lists.newArrayList());
            return po;
        }
        po.setOrgId(orgId);
        List<EditAppCertPO.AppCertPO> items = Lists.newArrayList();
        for (GetAppCertInfosReply.AppCertInfo certInfo : list) {
            EditAppCertPO.AppCertPO appCertPO = new EditAppCertPO.AppCertPO();
            appCertPO.setPushChannel(certInfo.getPushChannel());
            appCertPO.setAppChannel(certInfo.getAppChannel());
            appCertPO.setBundleId(certInfo.getBundleId());
            appCertPO.setDeveloperAppId(certInfo.getDeveloperAppId());
            appCertPO.setDeveloperSecretKey(certInfo.getDeveloperSecretKey());
            if (certInfo.getPushChannel().equalsIgnoreCase("apple")) {
                appCertPO.setPassword(certInfo.getPassword());
            } else if (certInfo.getPushChannel().equalsIgnoreCase("fcm")) {
                appCertPO.setFcmDatabaseUrl(certInfo.getDeveloperAppId());
                appCertPO.setDeveloperAppId("");
            }
            items.add(appCertPO);
        }
        po.setItems(items);
        return po;
    }

    public MessageReply editPushSwitch(long orgId, String switchType, boolean open, String adminUserName) {
        EditPushSwitchRequest request = EditPushSwitchRequest.newBuilder()
                .setOrgId(orgId)
                .setSwitchType(EditPushSwitchRequest.SwitchType.valueOf(switchType))
                .setOpen(open)
                .build();
        return smsSignClient.editPushSwitch(request, adminUserName);
    }

    public List<AppPushSwitchDTO> getAppPushSwitches(long orgId) {
        List<GetPushSwitchesReply.PushSwitch> list = smsSignClient.getPushSwitches(orgId, null);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }

        Map<Long, List<GetPushSwitchesReply.PushSwitch>> group = list.stream().collect(Collectors.groupingBy(GetPushSwitchesReply.PushSwitch::getOrgId));
        List<AppPushSwitchDTO> result = Lists.newArrayList();
        for (Long org : group.keySet()) {
            AppPushSwitchDTO dto = new AppPushSwitchDTO();
            dto.setOrgId(org);
            Broker broker = brokerMapper.getByBrokerId(org);
            if (broker != null) {
                dto.setOrgName(broker.getName());
            }
            for (GetPushSwitchesReply.PushSwitch pushSwitch : group.get(org)) {
                if (pushSwitch.getSwitchType() == GetPushSwitchesReply.SwitchType.ALL_SITE) {
                    dto.setAllSiteSwitchOpen(pushSwitch.getStatus() == 1);
                } else if (pushSwitch.getSwitchType() == GetPushSwitchesReply.SwitchType.CUSTOM) {
                    dto.setCustomSwitchOpen(pushSwitch.getStatus() == 1);
                }
            }
            result.add(dto);
        }
        return result;
    }

}
