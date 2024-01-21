package io.bhex.saas.admin.controller;

import com.google.api.client.util.Lists;
import com.google.gson.reflect.TypeToken;
import io.bhex.bhop.common.bizlog.BussinessLogAnnotation;
import io.bhex.bhop.common.bizlog.ExcludeLogAnnotation;
import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.broker.common.util.JsonUtil;
import io.bhex.saas.admin.service.impl.LetfService;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@ExcludeLogAnnotation
@RequestMapping("/api/v1/letf")
public class LetfController extends BaseController {

    @Resource
    private LetfService letfService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ResultModel list() {
        try {
            OkHttpClient httpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url("https://www.hbtc.co/api/basic/etf_price")
                    .get()
                    .build();

            Call call = httpClient.newCall(request);
            @Cleanup
            Response response = call.execute();
            List<EtfPrice> items = JsonUtil.defaultGson().fromJson(response.body().string(), new TypeToken<List<EtfPrice>>() {}.getType());
            if(CollectionUtils.isEmpty(items)) {
                return ResultModel.ok(Lists.newArrayList());
            }
            Map<String, BigDecimal> group = items.stream().collect(Collectors.toMap(e -> e.getSymbol(), e -> e.getPrice()));
            List<Map<String, Object>> list = letfService.getLetfInfos(items.stream().map(i -> i.getSymbol()).collect(Collectors.toList()));
            for (Map<String, Object> item : list) {
                item.put("price", group.get(item.get("symbolId")));
            }
            return ResultModel.ok(list);
        } catch (Exception e) {
            log.error("", e);
        }
        return ResultModel.ok();
    }

    @RequestMapping(value = "/operation_record", method = RequestMethod.GET)
    public ResultModel getOperationRecord(@RequestParam String symbolId) {
        return ResultModel.ok(letfService.getOperationList(symbolId));
    }

    @BussinessLogAnnotation(entityId = "{#po.symbolId}")
    @RequestMapping(value = "/switch_trade", method = RequestMethod.POST)
    public ResultModel switchLetfSymbolTrade(@RequestBody @Valid LetfOperation po) {
        if (po.getEnabled()) {
            letfService.allowTrade(po.getSymbolId());
        } else {
            letfService.stopTrade(po.getSymbolId());
        }
        return ResultModel.ok();
    }

    @BussinessLogAnnotation(entityId = "{#po.symbolId}")
    @RequestMapping(value = "/cancel_order", method = RequestMethod.POST)
    public ResultModel cancelLetfSymbolOrder(@RequestBody @Valid LetfOperation po) {
        letfService.cancelOrders(po.getSymbolId());
        return ResultModel.ok();
    }

    @BussinessLogAnnotation(entityId = "{#po.symbolId}")
    @RequestMapping(value = "/mergeBalance", method = RequestMethod.POST)
    public ResultModel mergeLetfSymbolBalance(@RequestBody @Valid LetfOperation po) {
        if (po.getMergeTimes() <= 1) {
            return ResultModel.error("mergeTimes more than 1");
        }
        BigDecimal mergeRate = BigDecimal.ONE.divide(new BigDecimal(po.getMergeTimes()), 18, RoundingMode.UP);
        if (mergeRate.stripTrailingZeros().toPlainString().length() > 16) {
            return ResultModel.error("mergeTimes like 1 2 4 8 10 20 50 100 1000 .....");
        }

        letfService.mergeBalance(po.getSymbolId(), mergeRate);
        return ResultModel.ok();
    }

    @Data
    private static class EtfPrice {
        private String symbol;
        private BigDecimal price;
    }

    @Data
    private static class LetfOperation {
        @NotEmpty
        private String symbolId;
        private String remark;
        private Integer mergeTimes;
        private Boolean enabled;
    }
}
