package io.bhex.saas.admin.service;

import io.bhex.bhop.common.util.Combo2;
import io.bhex.saas.admin.controller.dto.EditExchangePO;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.ExchangeInfo;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/10/6 下午12:43
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface ExchangeInfoService {

    List<ExchangeInfo> getExchangeInfoList(List<Long> exchangeIdList);

    /**
     * add exchange in bh-platform, exchange-gateway , exchange-admin
     * @param instanceId
     * @param exchangeInfo
     * @return Combo2 v1-result v2-如果result=false 指出出错原因
     */
    public Combo2<Boolean,String> addExchangeInfo(Long instanceId, ExchangeInfo exchangeInfo, BigDecimal saasFee, Integer cateogy);

//    /**
//     * 在exchange-admin中创建管理员账号并发送邮件
//     * @param exchangeId
//     */
//    public void createExchangeAdmin(Long exchangeId);

    public ExchangeInfo getExchangeInfoByExchangeName(String exchangeName);

    public ExchangeInfo getExchangeInfoById(Long id);

    public ExchangeInfo getExchangeInfoByExchangeId(Long id);

    /**
     * enable or disable exchange
     * @param id
     * @param newStatus
     * @return
     */
    public boolean updateExchangeStatus(Long id,int newStatus);

    boolean forbidAccess(Long exchangeId);

    public boolean editExchangeInfo(EditExchangePO editExchangePO, Integer category);

    List<ExchangeInfo> queryAllExchanges();

    public Combo2<List<ExchangeInfo>,Integer> queryExchangeInfos(int current, int pageSize, String exchangeName, Long exchangeId);


    ExchangeInstanceDetail getInstanceInfoByExchangeId(Long exchangeId);

    BigDecimal getSaasFee(Long exchangeId, Integer category);

    boolean updateExchangeInstanceStatus(Long exchangeId, Integer status);

    public boolean sendSetPasswordEmail(Long exchangeId);
}
