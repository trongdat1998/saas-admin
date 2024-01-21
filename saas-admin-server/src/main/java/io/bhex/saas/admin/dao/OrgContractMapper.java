package io.bhex.saas.admin.dao;

import io.bhex.saas.admin.model.OrgContract;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.dao
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 3:35 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Component
@org.apache.ibatis.annotations.Mapper
public interface OrgContractMapper extends Mapper<OrgContract> {

    String TABLE_NAME = " tb_org_contract ";

    String COLUMNS = " id, broker_id, exchange_id, contract_id, apply_org_id, broker_status, exchange_status, created_at ";

    @Select("SELECT count(id) FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and broker_status in (1, 2, 3, 4, 5)")
    int countBrokerContractByOrgId(@Param("brokerId") Long orgId);

    @Select("SELECT count(id) FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and broker_status in (0)")
    int countBrokerApplicationByOrgId(@Param("brokerId") Long orgId);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and broker_status in (1, 2, 3, 4, 5) limit #{fromindex}, #{endindex}")
    List<OrgContract> listBrokerContract(@Param("brokerId") Long brokerId, @Param("fromindex") Integer fromindex, @Param("endindex") Integer endindex);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and broker_status = 1")
    List<OrgContract> listAllBrokerContract(@Param("brokerId") Long brokerId);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and broker_status in (0) limit #{fromindex}, #{endindex}")
    List<OrgContract> listBrokerApplication(@Param("brokerId") Long brokerId, @Param("fromindex") Integer fromindex, @Param("endindex") Integer endindex);

    @Update("update " + TABLE_NAME + " set exchange_status = #{exchangeStatus}, broker_status = #{brokerStatus}, apply_org_id = #{brokerId} WHERE contract_id = #{contractId} and broker_id = #{brokerId}")
    int updateBrokerContractStatus(@Param("brokerId") Long brokerId, @Param("contractId") Long contractId, @Param("exchangeStatus") Integer exchangeStatus, @Param("brokerStatus") Integer brokerStatus);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE broker_id = #{brokerId} and contract_id = #{contractId}")
    OrgContract getBrokerContract(@Param("brokerId") Long brokerId, @Param("contractId") Long contractId);

    /**
     * 以下为exchange 相关
     */

    @Select("SELECT count(id) FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and exchange_status in (1, 2, 3, 4, 5)")
    int countExchangeContractByOrgId(@Param("exchangeId") Long orgId);

    @Select("SELECT count(id) FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and exchange_status in (0)")
    int countExchangeApplicationByOrgId(@Param("exchangeId") Long orgId);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and exchange_status in (1, 2, 3, 4, 5) limit #{fromindex}, #{endindex}")
    List<OrgContract> listExchangeContract(@Param("exchangeId") Long exchangeId, @Param("fromindex") Integer fromindex, @Param("endindex") Integer endindex);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and exchange_status = 1")
    List<OrgContract> listAllExchangeContract(@Param("exchangeId") Long exchangeId);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and exchange_status in (0) limit #{fromindex}, #{endindex}")
    List<OrgContract> listExchangeApplication(@Param("exchangeId") Long exchangeId, @Param("fromindex") Integer fromindex, @Param("endindex") Integer endindex);

    @Update("update " + TABLE_NAME + " set exchange_status = #{exchangeStatus}, broker_status = #{brokerStatus}, apply_org_id = #{exchangeId} WHERE exchange_id = #{exchangeId} and contract_id = #{contractId}")
    int updateExchangeContractStatus(@Param("exchangeId") Long exchangeId, @Param("contractId") Long contractId, @Param("exchangeStatus") Integer exchangeStatus, @Param("brokerStatus") Integer brokerStatus);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} and contract_id = #{contractId}")
    OrgContract getExchangeContract(@Param("exchangeId") Long exchangeId, @Param("contractId") Long contractId);

    @Select("SELECT " + COLUMNS + " FROM " + TABLE_NAME + " WHERE exchange_id = #{exchangeId} " +
            " and broker_id = #{brokerId} and exchange_status = 1 and broker_status = 1")
    OrgContract getOrgContractInContract(@Param("exchangeId") Long exchangeId, @Param("brokerId") Long brokerId);


}
