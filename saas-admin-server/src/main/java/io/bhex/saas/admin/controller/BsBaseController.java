package io.bhex.saas.admin.controller;

import io.bhex.base.token.TokenCategory;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.broker.grpc.account.SubAccount;
import io.bhex.broker.grpc.admin.GetBrokerUserRequest;
import io.bhex.broker.grpc.common.AccountTypeEnum;
import io.bhex.saas.admin.controller.dto.BrokerUserDTO;
import io.bhex.saas.admin.controller.param.GetBrokerUserPO;
import io.bhex.saas.admin.grpc.client.impl.BsUserClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BsBaseController extends BaseController {
    @Autowired
    private BsUserClient brokerUserClient;


    /**
     * @param po  可以转换成GetBrokerUserPO的Bean
     * @param <T>
     * @return v1-userid v2-accountid
     */
    protected <T> Combo2<Long, Long> getUserIdAndAccountId(T po) {
        return getUserIdAndAccountId(po, AccountTypeEnum.COIN);
    }
    protected <T> Combo2<Long, Long> getUserIdAndAccountId(T po, TokenCategory tokenCategory) {
        AccountTypeEnum accountType = AccountTypeEnum.COIN;
        if (tokenCategory == TokenCategory.FUTURE_CATEGORY) {
            accountType = AccountTypeEnum.FUTURE;
        }
        if (tokenCategory == TokenCategory.OPTION_CATEGORY) {
            accountType = AccountTypeEnum.OPTION;
        }
        return getUserIdAndAccountId(po, accountType);
    }
    protected <T> Combo2<Long, Long> getUserIdAndAccountId(T po, AccountTypeEnum accountType) {

        GetBrokerUserPO userPO = new GetBrokerUserPO();
        BeanUtils.copyProperties(po, userPO);
        if ((userPO.getUserId() == null || userPO.getUserId() == 0)
                && StringUtils.isEmpty(userPO.getPhone()) && StringUtils.isEmpty(userPO.getEmail())) {
            return null;
        }
        GetBrokerUserRequest.Builder builder = GetBrokerUserRequest.newBuilder();
        builder.setOrgId(userPO.getOrgId());
        if (userPO.getAccountId() != null && userPO.getAccountId() > 0) {
            builder.setAccountId(userPO.getAccountId());
        }
        if (userPO.getUserId() != null && userPO.getUserId() > 0) {
            builder.setUserId(userPO.getUserId());
        }
        if (StringUtils.isNotEmpty(userPO.getPhone())) {
            builder.setMobile(userPO.getPhone());
            if (StringUtils.isNoneEmpty(userPO.getNationalCode())) {
                builder.setNationalCode(userPO.getNationalCode());
            }
        }
        if (StringUtils.isNoneEmpty(userPO.getEmail())) {
            builder.setEmail(userPO.getEmail());
        }

        BrokerUserDTO dto = brokerUserClient.getBrokerUser(builder.build());
        if (dto == null) {
            return null;
        }
        SubAccount subAccount = brokerUserClient.queryMainAccount(userPO.getOrgId(), dto.getUserId(), accountType);
        return new Combo2<>(dto.getUserId(), subAccount != null ? subAccount.getAccountId() : 0L);
    }
}
