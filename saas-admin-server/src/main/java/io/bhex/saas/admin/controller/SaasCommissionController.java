package io.bhex.saas.admin.controller;

import io.bhex.bhop.common.controller.BaseController;
import io.bhex.bhop.common.dto.ExchangeCommissionDTO;
import io.bhex.bhop.common.dto.ExchangeCommissionDetailDTO;
import io.bhex.bhop.common.util.ResultModel;
import io.bhex.saas.admin.controller.dto.QueryExCommissionsPO;
import io.bhex.saas.admin.service.SaasCommissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/saas_commission")
public class SaasCommissionController extends BaseController {


    @Autowired
    private SaasCommissionService saasCommissionService;


    @RequestMapping(value = "/query_commissions", method = RequestMethod.POST)
    public ResultModel queryCommissions(@RequestBody @Valid QueryExCommissionsPO po) {

        List<ExchangeCommissionDTO> list =
                saasCommissionService.getExchangeCommissions(po.getFromTime(), po.getEndTime(), po.getExchangeName(),
                        po.getNext()==true ? po.getLastId() : po.getFromId(),
                        po.getNext(),
                        po.getPageSize());

        return ResultModel.ok(list);
    }





}