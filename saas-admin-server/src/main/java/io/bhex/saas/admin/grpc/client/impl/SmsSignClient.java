package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.common.*;
import io.bhex.bhop.common.config.GrpcConfig;
import io.bhex.broker.grpc.app_push.AppPushServiceGrpc;
import io.bhex.saas.admin.config.BrokerServerChannelRouter;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.model.SmsSign;
import io.grpc.Channel;
import io.grpc.Deadline;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SmsSignClient {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerServerChannelRouter brokerServerChannelRouter;

    public MessageServiceGrpc.MessageServiceBlockingStub getCommonServerStub() {
        return grpcConfig.messageServiceBlockingStub(GrpcConfig.COMMON_SERVER_CHANNEL_NAME);
    }

    public AppPushServiceGrpc.AppPushServiceBlockingStub getBrokerServerStub(long orgId) {
        Channel channel = brokerServerChannelRouter.getChannelByBrokerId(orgId);
        return AppPushServiceGrpc.newBlockingStub(channel)
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT));
    }

    public MessageReply createSmsSign(Long orgId, String brokerName, String sign) {
        CreateSmsSignRequest request = CreateSmsSignRequest.newBuilder()
                .setOrgId(orgId)
                .setOrgName(brokerName)
                .setSignName(sign)
                .addAllLanguage(Arrays.asList(new String[]{"en_US", "zh_CN"}))
                .build();
        MessageReply reply = getCommonServerStub().createSmsSign(request);
        log.info("reply:{}", reply);
        return reply;
    }

    public SmsSign getSmsSign(Long orgId) {
        List<SmsSign> signs = getSmsSigns(orgId, 0L, 0L,  1);
        if (CollectionUtils.isEmpty(signs)) {
            return null;
        }
        return signs.get(0);
    }

    public List<SmsSign> getSmsSigns(Long orgId, Long fromId, Long endId, Integer limit) {
        ListSmsSignsRequest request = ListSmsSignsRequest.newBuilder()
                .setFromId(fromId)
                .setEndId(endId)
                .setPageSize(limit)
                .setOrgId(orgId)
                .build();

        ListSmsSignsReply reply = getCommonServerStub().listSmsSigns(request);
        List<io.bhex.base.common.SmsSign> smsSigns = reply.getSmsSignList();

        if (CollectionUtils.isEmpty(smsSigns)) {
            return new ArrayList<>();
        }
        List<SmsSign> list = smsSigns.stream().map(s -> {
            SmsSign smsSign = new SmsSign();
            BeanUtils.copyProperties(s, smsSign);
            smsSign.setSign(s.getSignName());
            return smsSign;
        }).collect(Collectors.toList());

        return list;
    }


    public List<DeliveryRecord> getDeliveryRecords(String receiver) {
        ListDeliveryRecordsRequest request = ListDeliveryRecordsRequest.newBuilder()
                .setReceiver(receiver).build();
        return getCommonServerStub().listDeliveryRecords(request).getRecordList();
    }

    public MessageReply editAppCertInfo(EditAppCertInfoRequest request) {
        return getCommonServerStub().editAppCertInfo(request);
    }

    public List<GetAppCertInfosReply.AppCertInfo> getAppCertInfos(GetAppCertInfosRequest request) {
        return getCommonServerStub().getAppCertInfos(request).getAppCertInfoList();
    }

    public MessageReply editPushSwitch(EditPushSwitchRequest request, String adminUserName) {
        MessageReply reply = getCommonServerStub().editPushSwitch(request);
        io.bhex.broker.grpc.app_push.EditPushSwitchRequest req = io.bhex.broker.grpc.app_push.EditPushSwitchRequest.newBuilder()
                .setOrgId(request.getOrgId())
                .setOpen(request.getOpen())
                .setSwitchTypeValue(request.getSwitchTypeValue())
                .setAdminUserName(adminUserName)
                .build();
        getBrokerServerStub(request.getOrgId()).editPushSwitch(req);
        return reply;
    }

    public List<GetPushSwitchesReply.PushSwitch> getPushSwitches(long orgId, GetPushSwitchesRequest.SwitchType switchType) {
        GetPushSwitchesRequest.Builder builder = GetPushSwitchesRequest.newBuilder();
        if (orgId > 0) {
            builder.setOrgId(orgId);
        }
        if (switchType != null) {
            builder.setSwitchType(switchType);
        }
        return getCommonServerStub().getPushSwitches(builder.build()).getPushSwitchList();
    }
}
