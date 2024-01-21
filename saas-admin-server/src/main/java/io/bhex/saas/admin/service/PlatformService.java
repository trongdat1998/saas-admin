/*************************************
 * @项目名称: saas-admin-parent
 * @文件名称: PlatformService
 * @Date 2019/12/05
 * @Author fred.wang@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.service;

import com.google.protobuf.TextFormat;
import io.bhex.base.token.GetTokensReply;
import io.bhex.base.token.QueryExSymbolReply;
import io.bhex.base.token.QueryExSymbolRequest;
import io.bhex.base.token.SymbolAdminServiceGrpc;
import io.bhex.base.token.SymbolServiceGrpc;
import io.bhex.base.token.TokenDetail;
import io.bhex.base.token.UpdateExSymbolLimitPriceRequest;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.saas.admin.controller.dto.ExSymbolLimitPriceDTO;
import io.bhex.saas.admin.controller.dto.PlatformTokenDTO;
import io.bhex.saas.admin.controller.dto.SetExSymbolLimitPricePO;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.grpc.client.config.GrpcClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created on 2019/12/5
 *
 * @author wangxuefei
 */
@Slf4j
@Service
public class PlatformService {

    @Resource
    GrpcClientConfig grpcConfig;

    @Autowired
    private BrokerTokenClient exchangeTokenClient;

    private SymbolAdminServiceGrpc.SymbolAdminServiceBlockingStub symbolAdminServiceBlockingStub() {
        return grpcConfig.symbolAdminServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    private SymbolServiceGrpc.SymbolServiceBlockingStub symbolServiceBlockingStub() {
        return grpcConfig.symbolServiceBlockingStub(GrpcClientConfig.BH_SERVER_CHANNEL_NAME);
    }

    public PaginationVO<PlatformTokenDTO> queryPlatformTokens(Integer current, Integer pageSize, Integer tokenType, String tokenId) {

        GetTokensReply reply = exchangeTokenClient.getBhTokensNoCache(current, pageSize, null, tokenType, tokenId);

        List<TokenDetail> tokenDetails = reply.getTokenDetailsList();

        List<PlatformTokenDTO> list = new ArrayList<>();
        for (TokenDetail tokenDetail : tokenDetails) {
            PlatformTokenDTO dto = new PlatformTokenDTO();
            dto.setTokenId(tokenDetail.getTokenId());
            dto.setAllowDeposit(tokenDetail.getAllowDeposit());
            dto.setAllowWithdraw(tokenDetail.getAllowWithdraw());
            dto.setTokenType(tokenDetail.getTokenTypeValue());
            dto.setAddressNeedTag(tokenDetail.getAddressNeedTag());

            list.add(dto);
        }

        PaginationVO<PlatformTokenDTO> vo = new PaginationVO<>();
        vo.setCurrent(reply.getCurrent());
        vo.setPageSize(reply.getPageSize());
        vo.setTotal(reply.getTotal());
        vo.setList(list);

        return vo;
    }

    public List<ExSymbolLimitPriceDTO> queryExSymbols(Long exchangeId, String symbolId) {

        symbolId = symbolId == null ? "" : symbolId;

        try {
            QueryExSymbolReply reply = symbolAdminServiceBlockingStub().queryExSymbol(
                    QueryExSymbolRequest.newBuilder().setExchangeId(exchangeId).setSymbolId(symbolId).build()
            );

            return reply.getExSymbolsList().stream()
                    .map(ExSymbolLimitPriceDTO::fromProtoExSymbol)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("query ex symbol error {} {}", exchangeId, symbolId, e);
            return new ArrayList<>();
        }
    }

    public boolean setExSymbolLimitPrice(SetExSymbolLimitPricePO po) {

        UpdateExSymbolLimitPriceRequest request = UpdateExSymbolLimitPriceRequest.newBuilder()
                .setExchangeId(po.getExchangeId())
                .setSymbolId(po.getSymbolId())
                .setBuyMinPrice(Optional.ofNullable(po.getBuyMinPrice()).orElse(""))
                .setBuyMaxPrice(Optional.ofNullable(po.getBuyMaxPrice()).orElse(""))
                .setSellMinPrice(Optional.ofNullable(po.getSellMinPrice()).orElse(""))
                .setSellMaxPrice(Optional.ofNullable(po.getSellMaxPrice()).orElse(""))
                .setFromSaasAdmin(true)
                .build();

        try {
            symbolServiceBlockingStub().updateExSymbolLimitPrice(request);
            return true;
        } catch (Exception e) {
            log.error("updateExSymbolLimitPrice invoke bh error. {}", TextFormat.shortDebugString(request), e);
            return false;
        }
    }
}
