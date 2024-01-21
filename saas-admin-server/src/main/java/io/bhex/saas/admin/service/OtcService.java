package io.bhex.saas.admin.service;

import io.bhex.base.margin.cross.UpdateFundingCrossReply;
import io.bhex.saas.admin.controller.dto.MarginFundingCrossDTO;
import io.bhex.saas.admin.controller.param.UpdateOtcPaymentItemPO;

import java.util.List;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 16:38
 */
public interface OtcService {

    String queryPaymentItems();

    String setPaymentItems(UpdateOtcPaymentItemPO updatePo);
}
