package io.bhex.saas.admin.grpc.server;

import io.bhex.base.exadmin.BrokerSmsTemplateServiceGrpc;
import io.bhex.base.exadmin.GetSignsReply;
import io.bhex.base.exadmin.GetSignsRequest;
import io.bhex.base.exadmin.SignReply;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.model.SmsSign;
import io.bhex.saas.admin.service.SmsSignService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class BrokerSmsTemplateGrpcService extends BrokerSmsTemplateServiceGrpc.BrokerSmsTemplateServiceImplBase {

    @Autowired
    private SmsSignService smsSignService;
    @Override
    public void getSigns(GetSignsRequest request, StreamObserver<GetSignsReply> responseObserver) {
        GetSignsReply.Builder builder = GetSignsReply.newBuilder();
        List<SmsSign> list = smsSignService.selectSmsSignsByLastModify(request.getBrokerIdsList(), request.getLastModify());
        if(CollectionUtils.isEmpty(list)){
            builder.addAllSigns(new ArrayList<>());
        }
        else{
            List<SignReply> replies = list.stream().map(smsSign -> {
                SignReply.Builder signBuilder = SignReply.newBuilder();
                BeanUtils.copyProperties(smsSign, signBuilder);
                signBuilder.setUpdatedAt(smsSign.getUpdatedAt().getTime());
                signBuilder.setCreatedAt(smsSign.getCreatedAt().getTime());
                return signBuilder.build();
            }).collect(Collectors.toList());
            builder.addAllSigns(replies);
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }
}
