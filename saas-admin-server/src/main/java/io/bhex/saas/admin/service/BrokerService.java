package io.bhex.saas.admin.service;

import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.saas.admin.controller.dto.BrokerInfoRes;
import io.bhex.saas.admin.controller.dto.EditBrokerPO;
import io.bhex.saas.admin.enums.RegisterOptionType;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.BrokerInstanceDetail;

import java.util.List;
import java.util.Map;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service
 * @Author: ming.xu
 * @CreateDate: 15/08/2018 11:47 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface BrokerService {

    List<Broker> queryAllBrokers();

    Map<Long, String> queryBrokerName();

    /**
     * List Broker
     * @return
     */
    PaginationVO<BrokerInfoRes> queryBroker(int current, int pageSize, String brokerName, Long brokerId);

    /**
     * 查询券商
     * @param current
     * @param pageSize
     * @param brokerName
     * @param brokerId
     * @return
     */
    PaginationVO<BrokerInfoRes> queryBrokerTransferWhitelist(int current, int pageSize, String brokerName, Long brokerId);


    BrokerInfoRes queryBrokerDetail(Long brokerId);
    /**
     * get Broker by Id
     * @param id
     * @return
     */
    Broker getBrokerById(Long id);

    /**
     * get Broker by Id
     * @param brokerId
     * @return
     */
    Broker getBrokerByBrokerId(Long brokerId);

    /**
     * get Broker by brokerName
     * @param brokerName
     * @return
     */
    Broker getBrokerByBrokerName(String brokerName);

    /**
     * create Broker
     * @param broker
     * @return
     */
    Long createBroker(Broker broker, long dueTime);

   // public void createAdminUser(Long brokerId);
    /**
     * update Broker
     * @param po
     * @return
     */
    boolean updateBroker(EditBrokerPO po);

    /**
     * enable Broker
     * @param id
     * @param enabled
     * @return
     */
    boolean enableBroker(Long id, Boolean enabled);

    boolean openOtcShare(Long id);

    boolean cancelOtcShare(Long id);

    boolean forbidAccess(Long brokerId);

    boolean cancelforbidAccess(Long brokerId);
    /**
     * get Earnest Address
     * @return
     */
    String getEarnestAddress(Long id);

    BrokerInstanceDetail getInstanceInfoByBrokerId(Long brokerId);

    boolean updateBrokerInstanceStatus(Long brokerId, Integer status);

    boolean sendSetPasswordEmail(Long brokerId);

    boolean updateBrokeDueTimer(long brokerId, long dueTime);

    boolean updateBrokerRegisterOption(long id, RegisterOptionType type);
}
