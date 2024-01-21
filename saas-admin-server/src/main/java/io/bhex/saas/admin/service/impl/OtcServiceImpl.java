package io.bhex.saas.admin.service.impl;

import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.controller.param.UpdateOtcPaymentItemPO;
import io.bhex.saas.admin.service.OtcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author JinYuYuan
 * @description
 * @date 2020-08-18 16:43
 */
@Slf4j
@Service
public class OtcServiceImpl implements OtcService {


    @Override
    public String queryPaymentItems() {
        String reply = FeignConfig.getOtcClient().queryPaymentItems();
        return reply;
    }

    @Override
    public String setPaymentItems(UpdateOtcPaymentItemPO updatePo) {
        String reply = FeignConfig.getOtcClient().setPaymentItems(updatePo);
        return reply;
    }

}
