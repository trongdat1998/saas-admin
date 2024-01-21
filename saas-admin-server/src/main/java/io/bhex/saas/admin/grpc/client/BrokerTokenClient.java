package io.bhex.saas.admin.grpc.client;

import io.bhex.base.bhadmin.TokenApplyObj;
import io.bhex.base.bhadmin.TokenApplyRecordList;
import io.bhex.base.exadmin.TokenRecord;
import io.bhex.base.exadmin.TokenRecordList;
import io.bhex.base.quote.FairValue;
import io.bhex.base.token.GetTokensReply;
import io.bhex.base.token.QueryTokensReply;
import io.bhex.base.token.TokenDetail;
import io.bhex.base.token.TokenDetailInfo;
import io.bhex.broker.grpc.admin.*;
import io.bhex.broker.grpc.common.AdminSimplyReply;
import io.bhex.saas.admin.controller.dto.TokenRecordDTO;
import io.bhex.saas.admin.controller.param.TokenAuditPO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/11/4 上午11:11
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Service
public interface BrokerTokenClient {

    FairValue getFairValue(Long exchangeId, String tokenId);

    TokenDetailInfo getTokenDetailInfo(String tokenId);

    int setFairValue(Long exchangeId, String tokenId, BigDecimal usd);

    TokenApplyObj getTokenRecord(Long brokerId, String tokenId);

    TokenApplyObj getTokenRecordById(Long brokerId, Long tokenRecordId);

    //此接口读取bh数据无缓存
    GetTokensReply getBhTokensNoCache(Integer current, Integer pageSize, Integer category, Integer tokenType, String tokenId);

    QueryTokensReply queryBhTokens(Integer current, Integer pageSize, Integer category, Integer tokenType, String tokenId);

    boolean switchDepositWithdraw(String tokenId, Boolean allowDeposit, Boolean allowWithdraw, Boolean addressNeedTag);

    List<TokenDetail> getBhTokensByTokenIds(List<String> tokenIds);

    TokenDetail getBhToken(String tokenId);

    TokenApplyRecordList applicationList(Long brokerId, Integer current, Integer pageSize, int state, String token, String contractAddress);

    int auditTokenRecord(TokenAuditPO auditPO, String introduction, long applyBrokerId, long applyExchangeId);

    AddTokenReply initBrokerToken(long brokerId, String tokenId, int category);

    TokenPublishReply publishBrokerToken(long brokerId, String tokenId, int category, boolean published);

    AdminSimplyReply deleteBrokerToken(DeleteTokenRequest request);

    int changeTokenApplyBroker(String tokenId, Long exchangeId, Long toExchangeId, Long brokerId, Long toBrokerId);

    QueryTokenSimpleReply queryBrokerSimpleTokens(long brokerId, int category);

    QueryTokenReply queryBrokerTokens(Long brokerId, Integer current, Integer pageSize, Integer category, String tokenName);
}
