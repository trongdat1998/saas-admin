package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.bizlog.ExcludeLogAnnotation;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.param.InstanceInfoPO;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.OrgInstanceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.controller
 * @Author: ming.xu
 * @CreateDate: 09/08/2018 11:09 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@RestController
@ExcludeLogAnnotation
@RequestMapping("/api/v1/instance")
public class InstanceController {

    @Autowired
    private OrgInstanceService orgInstanceService;

    @Autowired
    private BrokerService brokerService;
    @Resource
    private Environment environment;

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/broker_list", method = RequestMethod.GET)
    public ResultModel listBrokerInstance(HttpServletRequest request) {
        List<BrokerInstanceDetail> brokerInstances = orgInstanceService.listBrokerInstances();
        return ResultModel.ok(brokerInstances);
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/exchange_list", method = RequestMethod.GET)
    public ResultModel listExchangeInstance(HttpServletRequest request) {
        List<ExchangeInstanceDetail> exchangeInstances = orgInstanceService.listExchangeInstances();
        return ResultModel.ok(exchangeInstances);
    }


    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/broker/host_info", method = RequestMethod.POST)
    public ResultModel getBrokerInstanceById(@RequestBody InstanceInfoPO po) {
        Broker broker = null;
        if (null != po.getBrokerId() && po.getBrokerId() != 0L) {
            broker = brokerService.getBrokerByBrokerId(po.getBrokerId());
        } else if (StringUtils.isNotEmpty(po.getBrokerName())) {
            broker = brokerService.getBrokerByBrokerName(po.getBrokerName());
        }
        if (null == broker) {
            return ResultModel.error("Broker not exist!");
        }
        Map<String, Object> result = new HashMap<>();
        BrokerInstanceDetail instanceInfo = orgInstanceService.getBrokerInstanceDetailByBrokerId(broker.getBrokerId());
        result.put("internalApiUrl", instanceInfo.getAdminInternalApiUrl());
        //result.put("port", instanceInfo.getPort());
        result.put("brokerId", broker.getBrokerId());
        return ResultModel.ok(result);
    }


    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/broker", method = RequestMethod.POST)
    public ResultModel createBrokerInstance() {
        return ResultModel.ok();
    }


    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/exchange/host_info", method = RequestMethod.POST)
    public ResultModel getexchangeInstanceById(@RequestBody InstanceInfoPO po) {
        Map<String, Object> instance = new HashMap<>();
        instance.put("host", environment.getProperty("exchange.admin.application-name", String.class));
        instance.put("port", environment.getProperty("exchange.admin.http-port", Integer.class));
        return ResultModel.ok(instance);
    }

    @AccessAnnotation(internal = true)
    @RequestMapping(value = "/exchange", method = RequestMethod.POST)
    public ResultModel createExchangeInstance() {
        return ResultModel.ok();
    }
}
