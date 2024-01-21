package io.bhex.saas.admin.grpc.server;

import io.bhex.base.bhadmin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.model.TokenApplyRecord;
import io.bhex.saas.admin.service.impl.AdminTokenApplyService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@GrpcService
public class AdminTokenApplyGrpcImpl extends AdminTokenApplyServiceGrpc.AdminTokenApplyServiceImplBase {

    @Autowired
    private AdminTokenApplyService tokenApplyService;

    @Override
    public void listTokenApplyRecords(GetTokenPager request, StreamObserver<TokenApplyRecordList> responseObserver) {
        responseObserver.onNext(tokenApplyService.listTokenApplyRecords(request));
        responseObserver.onCompleted();
    }

    @Override
    public void applyToken(TokenApplyObj request, StreamObserver<ApplyTokenResult> responseObserver) {
        TokenApplyRecord tokenRecord = TokenApplyRecord.parseFromProtoObj(request);
        int res = tokenApplyService.saveTokenRecord(tokenRecord);
        responseObserver.onNext(ApplyTokenResult.newBuilder().setRes(res).build());
        responseObserver.onCompleted();
    }

    @Override
    public void auditApplyToken(AuditTokenApplyRequest request, StreamObserver<AuditTokenApplyResult> responseObserver) {

    }


    @Override
    public void queryApplyTokenRecord(QueryApplyTokenRecordRequest request, StreamObserver<TokenApplyObj> responseObserver) {
        TokenApplyRecord tokenRecord = tokenApplyService.queryTokenRecord(request);
        responseObserver.onNext(TokenApplyRecord.toProtoObj(tokenRecord));
        responseObserver.onCompleted();
    }



    @Override
    public void changeTokenExchangeId(ChangeTokenExchangeIdRequest request, StreamObserver<ChangeTokenExchangeIdResult> responseObserver) {

    }

}
