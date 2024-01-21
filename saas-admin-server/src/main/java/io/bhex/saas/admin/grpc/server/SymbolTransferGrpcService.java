package io.bhex.saas.admin.grpc.server;


import io.bhex.base.bhadmin.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.saas.admin.service.impl.SymbolTransferService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@GrpcService
@Slf4j
public class SymbolTransferGrpcService extends AdminSymbolTransferServiceGrpc.AdminSymbolTransferServiceImplBase {

    @Autowired
    private SymbolTransferService symbolTransferService;

    @Override
    public void listSymbolTransferBySymbolIds(ListSymbolTransferRequest request, StreamObserver<ListSymbolTransferReply> responseObserver) {
        ListSymbolTransferReply reply = symbolTransferService.listSymbolTransferBySymbolIds(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void listSymbolTransferByMatchBrokerId(ListSymbolTransferByMatchBrokerIdRequest request, StreamObserver<ListSymbolTransferReply> responseObserver) {

    }

    @Override
    public void saveSymbolTransfer(SaveSymbolTransferRequest request, StreamObserver<SaveSymbolTransferReply> responseObserver) {
        SaveSymbolTransferReply reply = symbolTransferService.saveSymbolMatchTransfer(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void getSymbolTransferInfo(GetSymbolTransferInfoRequest request, StreamObserver<SymbolTransferInfo> responseObserver) {

    }

    @Override
    public void closeSymbolTransfer(CloseSymbolTransferRequest request, StreamObserver<SaveSymbolTransferReply> responseObserver) {
        SaveSymbolTransferReply reply = symbolTransferService.closeSymbolTransfer(request);
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void closeSymbolTransferToMe(CloseSymbolTransferToMeRequest request, StreamObserver<SaveSymbolTransferReply> responseObserver) {
        SaveSymbolTransferReply reply = symbolTransferService.closeTransferToMe(request.getBrokerId(), request.getSymbolId());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}
