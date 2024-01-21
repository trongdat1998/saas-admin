package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.param.IndexConfigPO;
import io.bhex.saas.admin.grpc.client.IQuoteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/quote")
public class QuoteIndexController {
    private final IQuoteClient quoteClient;

    @Autowired
    public QuoteIndexController(IQuoteClient quoteClient) {
        this.quoteClient = quoteClient;
    }

    @GetMapping("/index/list")
    public ResultModel listIndexConfigs(@RequestParam(required = false, defaultValue = "1") Integer page,
                                       @RequestParam(required = false, defaultValue = "50") Integer pageSize) {
        return ResultModel.ok(quoteClient.configList(page, pageSize));
    }

    @PostMapping("/index/save")
    public ResultModel saveConfigList(@RequestBody @Validated IndexConfigPO indexConfigPO) {
        quoteClient.saveIndexConfig(indexConfigPO);
        return ResultModel.ok();
    }
}
