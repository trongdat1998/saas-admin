/*************************************
 * @项目名称: saas-admin-parent
 * @文件名称: PlatformController
 * @Date 2019/12/05
 * @Author fred.wang@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.ExSymbolLimitPriceDTO;
import io.bhex.saas.admin.controller.dto.PlatformTokenDTO;
import io.bhex.saas.admin.controller.dto.QueryPlatformTokensPO;
import io.bhex.saas.admin.controller.dto.SetExSymbolLimitPricePO;
import io.bhex.saas.admin.grpc.client.BrokerTokenClient;
import io.bhex.saas.admin.service.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created on 2019/12/5
 *
 * @author wangxuefei
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/platform")
public class PlatformController {

    @Autowired
    private PlatformService platformService;
    @Autowired
    private BrokerTokenClient exchangeTokenClient;

    @RequestMapping(value = "/token/list")
    public ResultModel<PaginationVO<PlatformTokenDTO>> queryTokens(@RequestBody QueryPlatformTokensPO po) {

        PaginationVO<PlatformTokenDTO> vo = platformService.queryPlatformTokens(po.getCurrent(),
                po.getPageSize(), po.getTokenType(), po.getTokenId());
        return ResultModel.ok(vo);
    }

    @BussinessLogAnnotation(opContent = "tokenId:{#po.tokenId} allowDeposit:{#po.allowDeposit} allowWithdraw:{#po.allowWithdraw}")
    @PostMapping(value = "/token/switch_deposit_withdraw")
    public ResultModel switchDepositWithdraw(@RequestBody PlatformTokenDTO po) {

        boolean rst = exchangeTokenClient.switchDepositWithdraw(po.getTokenId(), po.getAllowDeposit(),
                po.getAllowWithdraw(), po.getAddressNeedTag());

        return rst ? ResultModel.ok("switch successfully") : ResultModel.error("switch failed");

    }

    @GetMapping(value = "/ex_symbol/limit_price/query")
    public ResultModel queryExSymbol(@RequestParam Long exchangeId,
                                     @RequestParam(required = false, defaultValue = "") String symbolId) {

        List<ExSymbolLimitPriceDTO> dtoList = platformService.queryExSymbols(exchangeId, symbolId);

        return ResultModel.ok(dtoList);
    }

    @PostMapping(value = "/ex_symbol/limit_price/update")
    public ResultModel setExSymbolLimitPrice(@RequestBody @Validated SetExSymbolLimitPricePO po) {

        boolean rst = platformService.setExSymbolLimitPrice(po);
        if (rst) {
            return ResultModel.ok();
        }

        return ResultModel.error("update ex_symbol limit price error");
    }

}
