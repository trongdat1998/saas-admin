/*************************************
 * @项目名称: saas-admin-parent
 * @文件名称: ExSymbolLimitPriceDTO
 * @Date 2019/12/16
 * @Author fred.wang@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.controller.dto;

import io.bhex.base.token.ExchangeSymbolDetail;
import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2019/12/16
 *
 * @author wangxuefei
 */
@Data
public class ExSymbolLimitPriceDTO implements Serializable {

    private Long exchangeId;

    private String symbolId;

    private String buyMinPrice;

    private String buyMaxPrice;

    private String sellMinPrice;

    private String sellMaxPrice;

    public static ExSymbolLimitPriceDTO fromProtoExSymbol(ExchangeSymbolDetail detail) {
        ExSymbolLimitPriceDTO dto = new ExSymbolLimitPriceDTO();
        dto.setExchangeId(detail.getOwnerExchangeId());
        dto.setSymbolId(detail.getSymbolId());
        dto.setBuyMinPrice(detail.getBuyMinPrice());
        dto.setBuyMaxPrice(detail.getBuyMaxPrice());
        dto.setSellMinPrice(detail.getSellMinPrice());
        dto.setSellMaxPrice(detail.getSellMaxPrice());
        return dto;
    }
}
