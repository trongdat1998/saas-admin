package io.bhex.saas.admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.bhex.base.bhadmin.AuditTokenApplyRequest;
import io.bhex.base.bhadmin.GetTokenPager;
import io.bhex.base.bhadmin.QueryApplyTokenRecordRequest;
import io.bhex.base.bhadmin.TokenApplyObj;
import io.bhex.base.bhadmin.TokenApplyRecordList;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.saas.admin.dao.TokenApplyMapper;
import io.bhex.saas.admin.model.TokenApplyRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminTokenApplyService {

    @Autowired
    private TokenApplyMapper mapper;

    @Transactional(rollbackFor = Throwable.class)
    public int saveTokenRecord(TokenApplyRecord tokenRecord) {
        log.info("tokenRecord:{}", tokenRecord);
//        ExchangeReply exchangeReply = bhOrgClient.findTrustExchangeByBrokerId(tokenRecord.getBrokerId());
//        if (exchangeReply == null) {
//            log.error("{} not trusted broker.", tokenRecord.getBrokerId());
//            return -3;
//        }
        if (StringUtils.isEmpty(tokenRecord.getTokenId()) || StringUtils.isEmpty(tokenRecord.getTokenName())) {
            return ErrorCode.ERR_REQUEST_PARAMETER.getCode();
        }
        Example tokenExample = new Example(TokenApplyRecord.class);
        tokenExample.createCriteria().andEqualTo("tokenId", tokenRecord.getTokenId());
        TokenApplyRecord tr = mapper.selectOneByExample(tokenExample);
        log.info("tokenRecord:{} dbRecord:{}", tokenRecord, tr);
        // 没有传ID认为要插入，如果有tr表示当前token已经存在
        if (Objects.nonNull(tr) && tokenRecord.getId() == 0) {
            return -1;
        }
        // 不存在，就
        if (Objects.isNull(tr)) {
            tokenRecord.setId(null);
            return mapper.insertSelective(tokenRecord);
        }
        if (tr.getState().equals(ApplyStateEnum.APPLYING.getState())) {
            return -2;
        }
        tr.setTokenType(tokenRecord.getTokenType());
        tr.setTokenName(tokenRecord.getTokenName());
        tr.setTokenFullName(tokenRecord.getTokenFullName());
        tr.setMaxWithdrawingAmt(tokenRecord.getMaxWithdrawingAmt());
        tr.setIntroduction(tokenRecord.getIntroduction());
        tr.setFairValue(tokenRecord.getFairValue());
        tr.setBrokerWithdrawingFee(tokenRecord.getBrokerWithdrawingFee());
        tr.setUpdateAt(new Date());
        tr.setIconUrl(tokenRecord.getIconUrl());
        tr.setMinDepositingAmt(tokenRecord.getMinDepositingAmt());
        tr.setMinWithdrawingAmt(tokenRecord.getMinWithdrawingAmt());
        tr.setMinPrecision(tokenRecord.getMinPrecision());
        tr.setState(ApplyStateEnum.APPLYING.getState());
        tr.setContractAddress(tokenRecord.getContractAddress());

        tr.setMaxQuantitySupplied(tokenRecord.getMaxQuantitySupplied());
        tr.setCurrentTurnover(tokenRecord.getCurrentTurnover());
        tr.setOfficialWebsiteUrl(tokenRecord.getOfficialWebsiteUrl());
        tr.setWhitePaperUrl(tokenRecord.getWhitePaperUrl());
        tr.setPublishTime(tokenRecord.getPublishTime());

        tr.setExtraTag(tokenRecord.getExtraTag());
        tr.setExtraConfig(tokenRecord.getExtraConfig());

        return mapper.updateByPrimaryKeySelective(tr);
    }



    public TokenApplyRecord queryTokenRecord(QueryApplyTokenRecordRequest request) {
        Example example = new Example(TokenApplyRecord.class);
        Example.Criteria criteria = example.createCriteria();
        if (request.getExchangeId() > 0) {
            criteria.andEqualTo("exchangeId", request.getExchangeId());
        }
        if (request.getBrokerId() > 0) {
            criteria.andEqualTo("brokerId", request.getBrokerId());
        }
        if (request.getId() > 0) {
            criteria.andEqualTo("id", request.getId());
        }
        if (StringUtils.isNotEmpty(request.getTokenId())) {
            criteria.andEqualTo("tokenId", request.getTokenId());
        }
        TokenApplyRecord tokenRecord = mapper.selectOneByExample(example);
        return tokenRecord;
    }

    public TokenApplyRecordList listTokenApplyRecords(GetTokenPager request) {
        Example example = new Example(TokenApplyRecord.class).excludeProperties("introduction");
        Page page = PageHelper.startPage(request.getStart(), request.getSize());
        Example.Criteria criteria = example.createCriteria();
        if (request.getBrokerId() > 0) {
            criteria.andEqualTo("brokerId", request.getBrokerId());
        }
        if (request.getTokenType() != -1) {
            criteria.andEqualTo("tokenType", request.getTokenType());
        }
        if (request.getState() != -1) {
            criteria.andEqualTo("state", request.getState());
        }
        if (StringUtils.isNotEmpty(request.getContractAddress())) {
            criteria.andEqualTo("contractAddress", request.getContractAddress());
        }
        if (StringUtils.isNotEmpty(request.getToken())) {
            Example.Criteria tokenCriteria = example.createCriteria();
            tokenCriteria.andLike("tokenName", "%" + request.getToken().toUpperCase() + "%")
                    .orLike("tokenId", "%" + request.getToken().toUpperCase() + "%")
                    .orLike("tokenFullName", "%" + request.getToken() + "%");
            example.and(tokenCriteria);
        }

        example.orderBy("updateAt").desc();
        List<TokenApplyObj> resultList = new ArrayList<>();
        List<TokenApplyRecord> tokenRecordList = mapper.selectByExample(example);
        if (Objects.nonNull(tokenRecordList)) {
            resultList = tokenRecordList
                    .stream()
                    .map(r -> TokenApplyRecord.toProtoObj(r))
                    .collect(Collectors.toList());
        }
        return TokenApplyRecordList.newBuilder()
                .addAllApplyRecord(resultList)
                .setTotal((int)page.getTotal())
                .build();

    }


    @Transactional
    public TokenApplyObj auditApplyToken(AuditTokenApplyRequest request) {
        log.info("auditTokenRecord:{}", request);
        TokenApplyRecord tokenRecord = mapper.selectByPrimaryKey(request.getId());
        if (Objects.nonNull(tokenRecord)) {
            if (tokenRecord.getState().equals(request.getCurState())) {
                tokenRecord.setState(request.getUpdatedState());
                tokenRecord.setFeeToken(request.getFeeToken());
                tokenRecord.setPlatformFee(DecimalUtil.toBigDecimal(request.getPlatformFee()));
                tokenRecord.setMinPrecision(request.getMinPrecision());
                tokenRecord.setConfirmCount(request.getConfirmCount());
                tokenRecord.setCanWithdrawConfirmCount(request.getCanWithdrawConfirmCount());
                tokenRecord.setReason(request.getReason());
                tokenRecord.setExploreUrl(request.getExploreUrl());
                tokenRecord.setUpdateAt(new Date());

                tokenRecord.setTokenType(request.getTokenType());
                tokenRecord.setTokenId(request.getTokenId());
                tokenRecord.setTokenName(request.getTokenName());
                tokenRecord.setTokenFullName(request.getTokenFullName());
                tokenRecord.setFairValue(DecimalUtil.toBigDecimal(request.getFairValue()));
                tokenRecord.setIconUrl(request.getIcoUrl());
                tokenRecord.setContractAddress(request.getContractAddress());
                tokenRecord.setIntroduction(request.getIntroduction());
                tokenRecord.setMinDepositingAmt(DecimalUtil.toBigDecimal(request.getMinDepositingAmt()));
                tokenRecord.setMinWithdrawingAmt(DecimalUtil.toBigDecimal(request.getMinWithdrawingAmt()));

                tokenRecord.setMaxQuantitySupplied(request.getMaxQuantitySupplied());
                tokenRecord.setCurrentTurnover(request.getCurrentTurnover());
                tokenRecord.setOfficialWebsiteUrl(request.getOfficialWebsiteUrl());
                tokenRecord.setWhitePaperUrl(request.getWhitePaperUrl());
                tokenRecord.setPublishTime(request.getPublishTime());
                tokenRecord.setIsPrivateToken(request.getIsPrivateToken() ? 1 : 0);
                tokenRecord.setIsAggregate(request.getIsAggregate() ? 1 : 0);
                tokenRecord.setIsTest(request.getIsTest() ? 1 : 0);
                tokenRecord.setIsBaas(request.getIsBaas() ? 1 : 0);
                tokenRecord.setChainName(request.getChainName());
                tokenRecord.setParentTokenId(request.getParentTokenId());
                tokenRecord.setChainSequence(request.getChainSequence());

                Example example = new Example(TokenApplyRecord.class);
                example.createCriteria()
                        .andEqualTo("id", tokenRecord.getId())
                        .andEqualTo("state", request.getCurState());
                int row = mapper.updateByExampleSelective(tokenRecord, example);
                log.info("auditTokenRecord row:{}", row);
            }
        }
        return TokenApplyRecord.toProtoObj(tokenRecord);
    }

    private static enum ApplyStateEnum {
        APPLYING(0),
        ACCEPT(1),
        REJECT(2);

        private int state;

        ApplyStateEnum(int state) {
            this.state = state;
        }

        public int getState() {
            return state;
        }
    }
}
