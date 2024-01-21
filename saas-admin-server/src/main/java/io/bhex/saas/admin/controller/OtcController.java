package io.bhex.saas.admin.controller;

import com.alibaba.fastjson.JSON;
import io.bhex.bhop.common.dto.param.OrgIdPO;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.OtcPaymentItemDTO;
import io.bhex.saas.admin.controller.param.OtcPaymentItemPO;
import io.bhex.saas.admin.controller.param.UpdateOtcPaymentItemPO;
import io.bhex.saas.admin.service.OtcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.util.StringUtil;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bingqing.yuan
 * @description
 * @date 2020-11-07 14:23
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/broker/otc")
public class OtcController {

    @Resource
    OtcService otcService;

    @RequestMapping(value = "/payment_items/list", method = RequestMethod.POST)
    public ResultModel queryPaymentItem() {
        String reply = otcService.queryPaymentItems();
        List<OtcPaymentItemPO.OtcPaymentItem> paymentItemList = new ArrayList<>();
        if (StringUtil.isNotEmpty(reply)) {
            List<OtcPaymentItemDTO> list = JSON.parseArray(reply, OtcPaymentItemDTO.class);
            list.forEach(item -> {
                OtcPaymentItemPO.OtcPaymentItem otcPaymentItem = new OtcPaymentItemPO.OtcPaymentItem();
                otcPaymentItem.setPaymentType(item.getPaymentType());
                otcPaymentItem.setLanguage(item.getLanguage());
                OtcPaymentItemPO.PaymentItem paymentItem = JSON.parseObject(item.getPaymentItems(), OtcPaymentItemPO.PaymentItem.class);
                otcPaymentItem.setPaymentItem(paymentItem);
                paymentItemList.add(otcPaymentItem);
            });
        }
        return ResultModel.ok(paymentItemList);
    }

    @RequestMapping(value = "/payment_items/set", method = RequestMethod.POST)
    public ResultModel setPaymentItem(@RequestBody @Validated OtcPaymentItemPO po) {
        List<OtcPaymentItemDTO> list = new ArrayList<>();
        for (OtcPaymentItemPO.OtcPaymentItem paymentItem : po.getPaymentItems()) {
            OtcPaymentItemDTO itemDTO = new OtcPaymentItemDTO();
            itemDTO.setPaymentType(paymentItem.getPaymentType());
            itemDTO.setLanguage(paymentItem.getLanguage());
            itemDTO.setPaymentItems(JSON.toJSONString(paymentItem.getPaymentItem()));
            list.add(itemDTO);
        }
        UpdateOtcPaymentItemPO updatePo = new UpdateOtcPaymentItemPO();
        updatePo.setData(JSON.toJSONString(list));
        String reply = otcService.setPaymentItems(updatePo);
        if (!reply.equalsIgnoreCase("success")) {
            ResultModel.error("set paymentItems failed.");
        }
        return ResultModel.ok();
    }
}
