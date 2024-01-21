package io.bhex.saas.admin.grpc.client.impl;

import io.bhex.base.account.*;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.saas.admin.constants.GrpcConstant;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import io.bhex.saas.admin.util.BigDecimalUtil;
import io.grpc.Deadline;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class GrpcBalanceClient {

    @Resource
    GrpcClientConfig grpcConfig;

    public BalanceServiceGrpc.BalanceServiceBlockingStub balanceServiceBlockingStub() {
        return grpcConfig.balanceServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    public LockBalanceReply lockBalance(LockBalanceRequest request) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = balanceServiceBlockingStub();
        try {
            BigDecimalUtil.checkParamScale(StringUtils.isNotEmpty(request.getLockAmount()) ? request.getLockAmount() : "");
            return stub.lockBalance(request);
        } catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }

    // 解锁锁仓
    public UnlockBalanceResponse unLockBalance(UnlockBalanceRequest request) {
        BalanceServiceGrpc.BalanceServiceBlockingStub stub = balanceServiceBlockingStub();
        try {
            BigDecimalUtil.checkParamScale(StringUtils.isNotEmpty(request.getUnlockAmount()) ? request.getUnlockAmount() : "");
            return stub.unlockBalance(request);
        } catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }

    public SyncTransferResponse syncTransfer(SyncTransferRequest request) {
        try {
            BigDecimalUtil.checkParamScale(StringUtils.isNotEmpty(request.getAmount()) ? request.getAmount() : "");
            BatchTransferServiceGrpc.BatchTransferServiceBlockingStub stub = grpcConfig.batchTransferServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
            return stub.withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT))
                    .syncTransfer(request);
        } catch (StatusRuntimeException e) {
            String error = String.format("code=%s, desc=%s, keys=%s",
                    e.getStatus().getCode(),
                    e.getStatus().getDescription(),
                    e.getTrailers() != null ? e.getTrailers().keys() : "trailers is null");
            log.error("{}", error);
            throw new BizException(ErrorCode.RPC_CALL_ERROR);
        }
    }

    public MergeBalanceResponse mergeBalance(MergeBalanceRequest request) {
        return grpcConfig.batchTransferServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME)
                .withDeadline(Deadline.after(GrpcConstant.DURATION_10_SECONDS, GrpcConstant.TIME_UNIT))
                .mergeBalance(request);
    }



}
