package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.util.ResultModel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.controller
 * @Author: ming.xu
 * @CreateDate: 08/08/2018 8:43 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@RestController
@RequestMapping("/")
public class TestController {

    @RequestMapping("/hello")
    public ResultModel hello(@RequestParam(value = "name") String name) {
        return ResultModel.ok(name);
    }
}
