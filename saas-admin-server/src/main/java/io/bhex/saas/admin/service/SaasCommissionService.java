package io.bhex.saas.admin.service;

import io.bhex.bhop.common.dto.ExchangeCommissionDTO;
import io.bhex.bhop.common.service.AdminCommissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SaasCommissionService {

    @Autowired
    private AdminCommissionService adminCommissionService;

    //@Scheduled(cron = "0 1 1-2 * * ?")
    public void loadData(){
        adminCommissionService.loadAllYesterdayExchangeCommissions();
    }

    public List<ExchangeCommissionDTO> getExchangeCommissions(Long fromTime, Long endTime, String exchangeName,
                                                              Long baseId, boolean next, Integer limit){
        List<ExchangeCommissionDTO> list = adminCommissionService
                .getExchangeCommissions(fromTime, endTime, exchangeName, baseId, next, limit);
        if(CollectionUtils.isEmpty(list)){
            return new ArrayList<>();
        }
        return list;
    }

//    public List<ExchangeCommissionDetailDTO> getExchangeCommissionDetails(Long exchangeId, Long exchangeCommissionId){
//        List<ExchangeCommissionDetailDTO> list = adminCommissionService
//                .getExchangeCommissionDetails(exchangeId, exchangeCommissionId);
//        if(CollectionUtils.isEmpty(list)){
//            return new ArrayList<>();
//        }
//
//        return list;
//    }

}
