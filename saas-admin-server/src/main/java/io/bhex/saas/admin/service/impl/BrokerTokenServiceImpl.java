package io.bhex.saas.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import io.bhex.base.account.ExchangeReply;
import io.bhex.base.bhadmin.TokenApplyObj;
import io.bhex.base.bhadmin.TokenApplyRecordList;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.base.token.*;
import io.bhex.base.token.TokenDetail;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.broker.grpc.admin.*;
import io.bhex.broker.grpc.common.AdminSimplyReply;
import io.bhex.saas.admin.controller.dto.BrokerTokenDTO;
import io.bhex.saas.admin.controller.dto.TokenRecordDTO;
import io.bhex.saas.admin.controller.param.TokenAuditPO;
import io.bhex.saas.admin.dao.ExchangeTokenMapper;
import io.bhex.saas.admin.dao.TokenApplyMapper;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.ExchangeToken;
import io.bhex.saas.admin.model.TokenApplyRecord;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.BrokerTokenService;
import io.bhex.saas.admin.service.OrgContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/5 上午10:31
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BrokerTokenServiceImpl implements BrokerTokenService {

    @Autowired
    private BrokerTokenClient brokerTokenClient;
    @Autowired
    private ExchangeTokenMapper exchangeTokenMapper;
    @Autowired
    private TokenApplyMapper tokenApplyMapper;
    @Autowired
    private OrgContractService orgContractService;
    @Autowired
    private ExchangeInfoService exchangeInfoService;
    @Autowired
    private BrokerService brokerService;
    @Autowired
    private BhOrgClient bhOrgClient;


    @Override
    public void syncExchangeTokens(Long exchangeId) {
        Integer current = 1;
        Integer pageSize = 23;
        Boolean isOver = false;

        while (!isOver) {
            GetTokensReply reply = brokerTokenClient.getBhTokensNoCache(current, pageSize, null, null, null);
            List<TokenDetail> tokenDetails = reply.getTokenDetailsList();
            if (CollectionUtils.isEmpty(tokenDetails)) {
                break;
            }

            for (TokenDetail tokenDetail : tokenDetails) {
                ExchangeToken exchangeToken = exchangeTokenMapper.getByExchangeIdAndToken(exchangeId, tokenDetail.getTokenId());
                if (exchangeToken == null) {
                    exchangeToken = new ExchangeToken();
                    BeanUtils.copyProperties(tokenDetail, exchangeToken);


                    exchangeToken.setExchangeId(exchangeId);
                    exchangeToken.setCategory(tokenDetail.getCategoryValue());
                    exchangeToken.setStatus(0);
                    exchangeToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    exchangeToken.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
                    log.info("token:{}", exchangeToken);
                    exchangeTokenMapper.insert(exchangeToken);
                }
            }
            isOver = pageSize > tokenDetails.size();
        }
    }

    private List<TokenApplyRecord> getApplyTokens(List<String> tokenIdList) {
        Example example = new Example(TokenApplyRecord.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("tokenId", tokenIdList);
        return tokenApplyMapper.selectByExample(example);
    }

    @Override
    public PaginationVO<BrokerTokenDTO> querySaasBrokerTokens(Long brokerId, Integer current, Integer pageSize, Integer category, String token) {
        QueryTokensReply reply = brokerTokenClient.queryBhTokens(current, pageSize, category, null, StringUtils.isEmpty(token) ? null : token.toUpperCase());

        long exchangeId = bhOrgClient.findExchangeByBrokerId(brokerId).getExchangeId();

        List<TokenDetail> tokenDetails = reply.getTokenDetailsList();
        //过滤掉私有币
        tokenDetails = tokenDetails.stream().filter(t -> !t.getIsPrivateToken()
            || (t.getIsPrivateToken() && t.getPrivateTokenExchangeId() == exchangeId)
        ).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(tokenDetails)) {
            return new PaginationVO<>();
        }

        List<SimpleToken> myTokens = brokerTokenClient.queryBrokerSimpleTokens(brokerId, category).getTokenDetailsList();

        List<BrokerTokenDTO> list = new ArrayList<>();
        for (TokenDetail detail : tokenDetails) {
            Optional<SimpleToken> brokerSimpleTokenOptional = myTokens.stream().filter(s -> s.getTokenId().equals(detail.getTokenId())).findFirst();
            SimpleToken simpleToken = brokerSimpleTokenOptional.isPresent() ? brokerSimpleTokenOptional.get() : null;

            BrokerTokenDTO dto = new BrokerTokenDTO();
            BeanUtils.copyProperties(detail, dto);
            dto.setIsPrivateToken(detail.getIsPrivateToken());
           // dto.setExchangeId(brokerId);
            dto.setBrokerId(brokerId);
            dto.setStatus(simpleToken != null ? 1 : 0);
            dto.setTokenName(simpleToken != null ? simpleToken.getTokenName() : detail.getTokenName());
            dto.setTokenFullName(simpleToken != null ? simpleToken.getTokenFullName() : detail.getTokenFullName());
            dto.setBrokerPublishStatus(simpleToken != null ? simpleToken.getStatus() : 0);
            list.add(dto);
        }

        PaginationVO<BrokerTokenDTO> vo = new PaginationVO<>();
        BeanUtils.copyProperties(reply, vo);
        vo.setList(list);
        return vo;

    }

    //int32 current = 1;//current page
    //    int32 page_size = 2;
    //    string token_name = 3;
    //    int64 broker_id = 4;
    //    int32 category = 5;
    //    string token_id = 6;
    @Override
    public PaginationVO<BrokerTokenDTO> queryMyBrokerTokens(Long brokerId, Integer current, Integer pageSize, Integer category, String tokenName) {
        QueryTokenReply brokerTokenReply = brokerTokenClient.queryBrokerTokens(brokerId, current, pageSize, category, tokenName);
        List<io.bhex.broker.grpc.admin.TokenDetail> brokerTokens = brokerTokenReply.getTokenDetailsList();
        List<String> myBrokerTokens = CollectionUtils.isEmpty(brokerTokens) ? Lists.newArrayList() :
                brokerTokens.stream()
                        //.filter(t -> t.getStatus() == 1)
                        .map(t -> t.getTokenId()).collect(Collectors.toList());

        List<TokenDetail> tokenDetails = brokerTokenClient.getBhTokensByTokenIds(myBrokerTokens);

        List<BrokerTokenDTO> list = new ArrayList<>();
        for (io.bhex.broker.grpc.admin.TokenDetail brokerToken : brokerTokens) {
            BrokerTokenDTO dto = new BrokerTokenDTO();
            Optional<TokenDetail> detailOptional = tokenDetails.stream().filter(d -> d.getTokenId().equals(brokerToken.getTokenId())).findFirst();
            if (detailOptional.isPresent()) {
                BeanUtils.copyProperties(detailOptional.get(), dto);
            } else {
                BeanUtils.copyProperties(brokerToken, dto);
                dto.setApplyBrokerId(0L);
            }
            dto.setCreatedAt(brokerToken.getCreated());
            dto.setTokenName(brokerToken.getTokenName());
            dto.setTokenFullName(brokerToken.getTokenFullName());
            dto.setBrokerId(brokerId);
            dto.setStatus(1);
            dto.setBrokerPublishStatus(brokerToken.getStatus());
            dto.setTokenDetail("");
            list.add(dto);
        }

        PaginationVO<BrokerTokenDTO> vo = new PaginationVO<>();
        vo.setPageSize(pageSize);
        vo.setTotal(brokerTokenReply.getTotal());
        vo.setCurrent(current);
        vo.setList(list);
        return vo;

    }

    /**
     * 更新状态，确认是否展示在交易所的币种中
     *
     * @param exchangeId
     * @param showInExchange
     * @return
     */
    @Override
    public boolean updateShowStatusInExchange(Long exchangeId, String tokenId, boolean showInExchange) {
        ExchangeToken exchangeToken = exchangeTokenMapper.getByExchangeIdAndToken(exchangeId, tokenId);

        if (exchangeToken == null) {
            GetTokensReply tokenDetailReply = brokerTokenClient.getBhTokensNoCache(1, 1, 1, null, tokenId);
            log.info("token detail : {}", tokenDetailReply);
            if (tokenDetailReply.getTokenDetailsCount() == 0) {
                throw new BizException(ErrorCode.TOKEN_MISSING);
            }
            TokenDetail tokenDetail = tokenDetailReply.getTokenDetailsList().get(0);
            if (tokenDetail.getTokenId().equals("")) {
                throw new BizException(ErrorCode.TOKEN_MISSING);
            }
            exchangeToken = new ExchangeToken();
            BeanUtils.copyProperties(tokenDetail, exchangeToken);
            if (StringUtils.isEmpty(exchangeToken.getTokenId())) {
                exchangeToken.setTokenId(tokenDetail.getTokenInfo().getTokenId());
            }
            if (StringUtils.isEmpty(exchangeToken.getTokenFullName())) {
                exchangeToken.setTokenFullName(tokenDetail.getTokenInfo().getTokenFullName());
            }
            exchangeToken.setExchangeId(exchangeId);
            exchangeToken.setCategory(tokenDetail.getCategoryValue());
            exchangeToken.setStatus(showInExchange ? 1 : 0);
            exchangeToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            exchangeToken.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            log.info("token:{}", exchangeToken);
            exchangeTokenMapper.insert(exchangeToken);
            TokenApplyObj tokenRecord = brokerTokenClient.getTokenRecord(-1L, tokenId);
            if (tokenRecord.getId() != 0) {
                brokerTokenClient.setFairValue(exchangeId, tokenId, DecimalUtil
                    .toBigDecimal(tokenRecord.getFairValue()));
                log.info("Add [{}:{}] fair value [{}]", tokenRecord.getExchangeId(), tokenRecord.getTokenId(),
                    tokenRecord.getFairValue());
            } else {
                log.error("Token: {} missing tokenRecord");
            }
        } else {
            exchangeTokenMapper.updateStatus(exchangeId, tokenId, showInExchange ? 1 : 0);
        }

        return true;
    }

    @Override
    public QueryTokensReply queryExchangeTokens(List<Long> exchangeIds, String tokenId, Integer current, Integer pageSize, Integer category) {
        Integer total = exchangeTokenMapper.countExchangeTokens(exchangeIds, tokenId, category);
        if (total == 0) {
            return QueryTokensReply.newBuilder().build();
        }
        QueryTokensReply.Builder builder = QueryTokensReply.newBuilder();
        current = current < 1 ? 1 : current;
        int start = (current - 1) * pageSize;
        if (start >= total) {
            return QueryTokensReply.newBuilder().build();
        }
        log.info("{} {} {} {}", tokenId, start, pageSize, category);
        List<String> tokenIds = exchangeTokenMapper.queryExchangeTokenIds(exchangeIds, tokenId, start, pageSize, category);
        builder.setTotal(total);
        builder.setCurrent(current);
        builder.setPageSize(pageSize);


        List<TokenDetail> tokenDetails = brokerTokenClient.getBhTokensByTokenIds(tokenIds);
//        List<TokenDetail> tokenDetails = new ArrayList<>();
//        for(String tokenid : tokenIds){
//            TokenDetail tokenDetail = exchangeTokenClient.getToken(tokenid);
//            tokenDetails.add(tokenDetail);
//        }
        builder.addAllTokenDetails(tokenDetails);
        return builder.build();
    }

    @Override
    public QueryTokensReply queryExchangeTokens(Long exchangeId, String tokenId, Integer current, Integer pageSize, Integer category) {
        List<Long> exchangeIds = new ArrayList<>();
        exchangeIds.add(exchangeId);
        return queryExchangeTokens(exchangeIds, tokenId, current, pageSize, category);
    }

    @Override
    public PaginationVO<TokenRecordDTO> applicationList(Long brokerId, Integer current, Integer pageSize, int state, String token, String contractAddress) {
        TokenApplyRecordList tokenRecordList = brokerTokenClient.applicationList(brokerId, current, pageSize, state, token, contractAddress);
        PaginationVO<TokenRecordDTO> tokenRecordDTOPaginationVO = new PaginationVO<>();
        tokenRecordDTOPaginationVO.setPageSize(pageSize);
        tokenRecordDTOPaginationVO.setCurrent(current);
        tokenRecordDTOPaginationVO.setTotal(tokenRecordList.getTotal());
        tokenRecordDTOPaginationVO.setList(tokenRecordList.getApplyRecordList()
            .stream()
            .map(TokenRecordDTO::parseTokenRecord)
            .collect(Collectors.toList()));
        List<Long> brokerIdList = tokenRecordDTOPaginationVO.getList().stream()
            .map(TokenRecordDTO::getBrokerId)
            .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(brokerIdList)) {
            List<Broker> brokerList = brokerService.queryAllBrokers();
            for (TokenRecordDTO tokenRecordDTO : tokenRecordDTOPaginationVO.getList()) {
                for (Broker broker : brokerList) {
                    if (tokenRecordDTO.getBrokerId().equals(broker.getBrokerId())) {
                        tokenRecordDTO.setOrgName(broker.getName());
                        break;
                    }
                }
            }
        }
        return tokenRecordDTOPaginationVO;
    }

    @Override
    public int auditTokenRecord(TokenAuditPO auditPO, long applyBrokerId) {
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(applyBrokerId);
        long applyExchangeId = exchangeReply.getExchangeId();
        String introduction = !CollectionUtils.isEmpty(auditPO.getIntroductions())
            ? JSON.toJSONString(auditPO.getIntroductions()) : "";
        int res = brokerTokenClient.auditTokenRecord(auditPO, introduction, applyBrokerId, applyExchangeId);
        if (res == -1) {
            throw new BizException(ErrorCode.TOKEN_ALREADY_EXIST);
        }
        return res;
    }

    @Override
    public TokenRecordDTO getTokenDetailInfo(Long brokerId, String tokenId) {
        TokenApplyObj tokenRecord = brokerTokenClient.getTokenRecord(-1L, tokenId);
        return TokenRecordDTO.parseTokenDetail(brokerTokenClient.getTokenDetailInfo(tokenId),
            brokerTokenClient.getFairValue(tokenRecord.getExchangeId(), tokenId),
            tokenRecord);
    }

    @Override
    public TokenDetailInfo getBhTokenInfo(String tokenId) {
        return brokerTokenClient.getTokenDetailInfo(tokenId);
    }

    @Override
    public TokenRecordDTO getTokenRecord(Long brokerId, String tokenId) {

        TokenApplyObj tokenRecord = brokerTokenClient.getTokenRecord(-1L, tokenId);
        return TokenRecordDTO.parseTokenRecord(tokenRecord);
    }

    @Override
    public TokenRecordDTO getTokenRecordById(Long brokerId, Long tokenRecordId) {
        TokenApplyObj tokenRecord = brokerTokenClient.getTokenRecordById(-1L, tokenRecordId);
        return TokenRecordDTO.parseTokenRecord(tokenRecord);
    }

    @Override
    public void changeTokenBroker(Long brokerId, String tokenId, Long toBrokerId) {
        Example example = new Example(TokenApplyRecord.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("tokenId", tokenId).andEqualTo("brokerId", brokerId);
        TokenApplyRecord applyRecord = tokenApplyMapper.selectOneByExample(example);
        ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(toBrokerId);
        brokerTokenClient.changeTokenApplyBroker(tokenId, applyRecord.getExchangeId(), exchangeReply.getExchangeId(), brokerId, toBrokerId);

        applyRecord.setBrokerId(toBrokerId);
        applyRecord.setExchangeId(exchangeReply.getExchangeId());
        applyRecord.setUpdateAt(new Date());
        int row = tokenApplyMapper.updateByPrimaryKeySelective(applyRecord);
        if (row < 1) {
            throw new BizException(ErrorCode.TOKEN_MISSING);
        }
    }

    @Override
    public AddTokenReply initBrokerToken(long brokerId, String tokenId, int category) {
        if (category == TokenCategory.MAIN_CATEGORY_VALUE) {
            TokenApplyObj tokenRecord = brokerTokenClient.getTokenRecord(-1L, tokenId);
            if (tokenRecord.getId() != 0) {
                ExchangeReply exchangeReply = bhOrgClient.findExchangeByBrokerId(brokerId);
                brokerTokenClient.setFairValue(exchangeReply.getExchangeId(), tokenId, DecimalUtil
                        .toBigDecimal(tokenRecord.getFairValue()));
                log.info("Add [{}:{}] fair value [{}]", exchangeReply.getExchangeId(), tokenRecord.getTokenId(),
                        tokenRecord.getFairValue());
            } else {
                log.error("{} no fair value", tokenId);
                throw new BizException(ErrorCode.TOKEN_MISSING);
            }
        }
        return brokerTokenClient.initBrokerToken(brokerId, tokenId, category);
    }

    @Override
    public AdminSimplyReply deleteBrokerToken(long brokerId, String tokenId) {
        DeleteTokenRequest request = DeleteTokenRequest.newBuilder().setOrgId(brokerId).setTokenId(tokenId).build();
        return brokerTokenClient.deleteBrokerToken(request);
    }
}
