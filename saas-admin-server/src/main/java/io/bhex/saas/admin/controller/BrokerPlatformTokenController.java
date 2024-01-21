package io.bhex.saas.admin.controller;

import com.google.common.collect.Maps;
import io.bhex.base.proto.BaseRequest;
import io.bhex.base.token.GetBrokerTokensReply;
import io.bhex.base.token.GetBrokerTokensRequest;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.jwt.filter.AccessAnnotation;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.bhop.common.util.validation.ValidUtil;
import io.bhex.saas.admin.controller.dto.BrokerTokenDTO;
import io.bhex.saas.admin.controller.dto.PlatformBrokerTokenDTO;
import io.bhex.saas.admin.controller.param.QueryPlatformBrokerTokensPO;
import io.bhex.saas.admin.grpc.client.impl.BrokerTokenClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.service.BrokerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/broker_token")
public class BrokerPlatformTokenController {

    @Autowired
    private BrokerTokenClient brokerTokenClient;
    @Autowired
    private BrokerService brokerService;

    @AccessAnnotation(authIds = {1801}) //平台币种管理使用
    @RequestMapping(value = "/query")
    public ResultModel<BrokerTokenDTO> queryBrokerTokens(@RequestBody QueryPlatformBrokerTokensPO po) {
        if (StringUtils.isNotEmpty(po.getToken())
                && !ValidUtil.isTokenName(po.getToken())) {
            return  ResultModel.ok();
        }
        GetBrokerTokensRequest.Builder builder = GetBrokerTokensRequest.newBuilder();
        if (po.getExchangeId() != null) {
            builder.setExchangeId(po.getExchangeId());
        }
        if (po.getBrokerId() != null) {
            builder.setBrokerId(po.getBrokerId());
        }
        if (StringUtils.isNotEmpty(po.getToken())) {
            builder.setToken(po.getToken());
        }
        if (StringUtils.isNotEmpty(po.getTokenId())) {
            builder.setTokenId(po.getTokenId());
        }
        builder.setCurrent(po.getCurrent());
        builder.setPageSize(po.getPageSize());
        builder.setCategory(po.getCategory());
        GetBrokerTokensReply reply = brokerTokenClient.queryBrokerTokens(builder.setBaseRequest(BaseRequest.newBuilder().setOrganizationId(21L).build()).build());

        List<Broker> brokers = brokerService.queryAllBrokers();
        Map<Long, String> brokerNameMap = Maps.newHashMap();
        brokers.forEach(b -> brokerNameMap.put(b.getBrokerId(), b.getName()));
        List<PlatformBrokerTokenDTO> items = reply.getBrokerTokenList().stream().map(bt -> {
            PlatformBrokerTokenDTO item = new PlatformBrokerTokenDTO();
            BeanUtils.copyProperties(bt, item);
            item.setBrokerId(bt.getOrgId());
            item.setBrokerName(brokerNameMap.getOrDefault(bt.getOrgId(), ""));
            return item;
        }).collect(Collectors.toList());

        PaginationVO<PlatformBrokerTokenDTO> vo = new PaginationVO<>();
        vo.setTotal(reply.getTotal());
        vo.setPageSize(po.getPageSize());
        vo.setCurrent(po.getCurrent());
        vo.setList(items);

        return ResultModel.ok(vo);
    }



}
