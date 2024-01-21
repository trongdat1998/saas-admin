package io.bhex.saas.admin.service;

import io.bhex.base.token.QueryTokensReply;
import io.bhex.base.token.TokenDetailInfo;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.broker.grpc.admin.AddTokenReply;
import io.bhex.broker.grpc.common.AdminSimplyReply;
import io.bhex.saas.admin.controller.dto.BrokerTokenDTO;
import io.bhex.saas.admin.controller.dto.TokenRecordDTO;
import io.bhex.saas.admin.controller.param.TokenAuditPO;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/11/5 下午4:40
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface BrokerTokenService {

    void syncExchangeTokens(Long exchangeId);

    PaginationVO<BrokerTokenDTO> querySaasBrokerTokens(Long brokerId, Integer current, Integer pageSize, Integer category, String token);

    PaginationVO<BrokerTokenDTO> queryMyBrokerTokens(Long exchangeId, Integer current, Integer pageSize, Integer category, String tokenName);

    boolean updateShowStatusInExchange(Long exchangeId, String tokenId, boolean showInExchange);

    QueryTokensReply queryExchangeTokens(List<Long> exchangeIds, String tokenId, Integer current, Integer pageSize, Integer category);

    QueryTokensReply queryExchangeTokens(Long exchangeId, String tokenId, Integer current, Integer pageSize, Integer category);

    PaginationVO<TokenRecordDTO> applicationList(Long brokerId, Integer current, Integer pageSize, int state, String token, String contractAddress);

    int auditTokenRecord(TokenAuditPO auditPO, long applyExchangeId);

    TokenRecordDTO getTokenDetailInfo(Long brokerId, String tokenId);

    TokenDetailInfo getBhTokenInfo(String tokenId);

    TokenRecordDTO getTokenRecord(Long brokerId, String tokenId);

    TokenRecordDTO getTokenRecordById(Long brokerId, Long tokenRecordId);

    void changeTokenBroker(Long brokerId, String tokenId, Long toBrokerId);

    AddTokenReply initBrokerToken(long brokerId, String tokenId, int category);

    AdminSimplyReply deleteBrokerToken(long brokerId, String tokenId);
}
