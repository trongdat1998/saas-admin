package io.bhex.saas.admin.controller;

import io.bhex.base.account.BusinessSubject;
import io.bhex.base.token.TokenCategory;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.BalanceFlowDTO;
import io.bhex.saas.admin.controller.param.QueryBalanceFlowsListPO;
import io.bhex.saas.admin.grpc.client.impl.BsBalanceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:财务相关
 * @Date: 2018/9/23 下午2:37
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/balance")
public class BsBalanceController extends BsBaseController {

    @Autowired
    private BsBalanceClient balanceClient;


    @RequestMapping(value = "/flows", method = RequestMethod.POST)
    public ResultModel queryFlows(@RequestBody @Valid QueryBalanceFlowsListPO po) {

        Combo2<Long, Long> combo2;
        if (po.getOrgId() != 21) {
            combo2 = getUserIdAndAccountId(po, TokenCategory.forNumber(po.getCategory()));
        } else { //系统账号只能写系统账户accountid
            combo2 = new Combo2<>(po.getUserId(), po.getUserId());
        }

        if (combo2 == null || combo2.getV2() == null) {
            return ResultModel.ok(new ArrayList<>());
        }

        List<BalanceFlowDTO> list = balanceClient.getBalanceFlows(9001L, combo2.getV2(),
                        po.getBusinessSubject() != null && po.getBusinessSubject() > 0
                                ? BusinessSubject.forNumber(po.getBusinessSubject()) : null,
                        po.getTokenId(),
                        po.getNext() ? po.getFromId() : 0,
                        po.getPageSize());
        return ResultModel.ok(list);
    }

}
