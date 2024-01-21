package io.bhex.saas.admin.grpc.server;

import io.bhex.base.admin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.service.OrgContractService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.server
 * @Author: ming.xu
 * @CreateDate: 01/11/2018 3:26 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@GrpcService
public class OrgContractGrpcService extends AdminOrgContractServiceGrpc.AdminOrgContractServiceImplBase {

    @Autowired
    private OrgContractService orgContractService;

    @Override
    public void listContract(ListContractRequest request, StreamObserver<ListContractReply> responseObserver) {
        ListContractReply reply = orgContractService.listOrgContract(request.getOrgId(), request.getApplyOrgType(), request.getCurrent(), request.getPageSize());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listContractApplication(ListContractRequest request, StreamObserver<ListContractReply> responseObserver) {
        ListContractReply reply = orgContractService.listApplication(request.getOrgId(), request.getApplyOrgType(), request.getCurrent(), request.getPageSize());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void addApplication(AddContractRequest request, StreamObserver<AddContractReply> responseObserver) {
        Boolean isOk = orgContractService.addApplication(request);
        AddContractReply reply = AddContractReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void updateContactInfo(UpdateContactInfoRequest request, StreamObserver<UpdateContactInfoReply> responseObserver) {
        Boolean isOk = orgContractService.editContactInfo(request);
        UpdateContactInfoReply reply = UpdateContactInfoReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void enableApplication(ChangeContractRequest request, StreamObserver<ChangeContractReply> responseObserver) {
        Boolean isOk = orgContractService.enableApplication(request.getOrgId(), request.getContractId(), request.getApplyOrgType());
        ChangeContractReply reply = ChangeContractReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void rejectApplication(ChangeContractRequest request, StreamObserver<ChangeContractReply> responseObserver) {
        Boolean isOk = orgContractService.rejectApplication(request.getOrgId(), request.getContractId(), request.getApplyOrgType());
        ChangeContractReply reply = ChangeContractReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void closeContract(ChangeContractRequest request, StreamObserver<ChangeContractReply> responseObserver) {
        Boolean isOk = orgContractService.closeOrgContract(request.getOrgId(), request.getContractId(), request.getApplyOrgType());
        ChangeContractReply reply = ChangeContractReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void reopenContract(ChangeContractRequest request, StreamObserver<ChangeContractReply> responseObserver) {
        Boolean isOk = orgContractService.reopenOrgContract(request.getOrgId(), request.getContractId(), request.getApplyOrgType());
        ChangeContractReply reply = ChangeContractReply.newBuilder()
                .setResult(isOk)
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listAllContract(ListAllContractRequest request, StreamObserver<ListContractReply> responseObserver) {
        try {
            ListContractReply reply = orgContractService.listAllOrgContractInfo(Arrays.asList(request.getOrgId()), request.getApplyOrgType());
            log.info("request:{} response:{}", request, reply);
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("listAllContract error", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listAllContractByOrgIds(ListAllContractByOrgIdsRequest request, StreamObserver<ListContractReply> responseObserver) {
        try {
            ListContractReply reply = orgContractService.listAllOrgContractInfo(request.getOrgIdsList(), request.getApplyOrgType());
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        } catch (Throwable e) {
            log.error("listAllContractByOrgIds error", e);
            responseObserver.onError(e);
        }
    }
}
