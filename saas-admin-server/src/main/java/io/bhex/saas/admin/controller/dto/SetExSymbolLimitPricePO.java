/*************************************
 * @项目名称: saas-admin-parent
 * @文件名称: SetExSymbolLimitPricePO
 * @Date 2019/12/16
 * @Author fred.wang@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * Created on 2019/12/16
 *
 * @author wangxuefei
 */
@Data
public class SetExSymbolLimitPricePO {

    @NotNull
    private Long exchangeId;

    @NotNull
    private String symbolId;

    private String buyMinPrice;

    private String buyMaxPrice;

    private String sellMinPrice;

    private String sellMaxPrice;
}
