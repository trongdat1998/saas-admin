package io.bhex.saas.admin.service.impl;

import io.bhex.base.account.ConfigBrokerExchangeContractRequest;
import io.bhex.base.admin.*;
import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import io.bhex.bhop.common.util.PageUtil;
import io.bhex.saas.admin.dao.OrgContractDetailMapper;
import io.bhex.saas.admin.dao.OrgContractMapper;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.ExchangeInfo;
import io.bhex.saas.admin.model.OrgContract;
import io.bhex.saas.admin.model.OrgContractDetail;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.OrgContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 31/10/2018 5:55 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class OrgContractServiceImpl implements OrgContractService {
    @Autowired
    private SnowflakeGenerator idGenerator;

    @Autowired
    private BhOrgClient bhOrgClient;

    @Autowired
    private OrgContractMapper orgContractMapper;

    @Autowired
    private OrgContractDetailMapper orgContractDetailMapper;

    @Autowired
    private ExchangeInfoService exchangeInfoService;

    @Autowired
    private BrokerService brokerService;

    @Override
    public List<OrgContractDetail> getContractDetailList(List<Long> orgIdList) {
        Example example = new Example(OrgContractDetail.class);
        example.createCriteria()
            .andEqualTo("orgType", OrgContract.EXCHANGE_RG)
            .andIn("orgId", orgIdList);
        return orgContractDetailMapper.selectByExample(example);
    }

    @Override
    public ListContractReply listOrgContract(Long orgId, OrgType orgType, Integer current, Integer pageSize) {
        int total = 0;
        Integer orgTypeInt = 0;
        List<OrgContract> orgContracts = null;
        if (orgType.equals(OrgType.Broker_Org)) {
            total = orgContractMapper.countBrokerContractByOrgId(orgId);
            PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);
            orgContracts = orgContractMapper.listBrokerContract(orgId, page.getStart(), page.getOffset());
            orgTypeInt = OrgContract.BROKER_ORG;
        } else if (orgType.equals(OrgType.Exchange_Org)) {
            total = orgContractMapper.countExchangeContractByOrgId(orgId);
            PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);
            orgContracts = orgContractMapper.listExchangeContract(orgId, page.getStart(), page.getOffset());
            orgTypeInt = OrgContract.EXCHANGE_RG;
        }
        ListContractReply.Builder builder = ListContractReply.newBuilder();
        builder.setCurrent(current)
                .setPageSize(pageSize)
                .setTotal(total);
        return processDate(Arrays.asList(orgId), orgTypeInt, orgContracts, builder);
    }

    @Override
    public ListContractReply listApplication(Long orgId, OrgType orgType, Integer current, Integer pageSize) {
        int total = 0;
        Integer orgTypeInt = 0;
        List<OrgContract> orgContracts = null;
        if (OrgType.Broker_Org.equals(orgType)) {
            total = orgContractMapper.countBrokerApplicationByOrgId(orgId);
            PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);
            orgContracts = orgContractMapper.listBrokerApplication(orgId, page.getStart(), page.getOffset());
            orgTypeInt = OrgContract.BROKER_ORG;
        } else if (OrgType.Exchange_Org.equals(orgType)) {
            total = orgContractMapper.countExchangeApplicationByOrgId(orgId);
            PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);
            orgContracts = orgContractMapper.listExchangeApplication(orgId, page.getStart(), page.getOffset());
            orgTypeInt = OrgContract.EXCHANGE_RG;
        }
        ListContractReply.Builder builder = ListContractReply.newBuilder();
        builder.setCurrent(current)
                .setPageSize(pageSize)
                .setTotal(total);
        return processDate(Arrays.asList(orgId), orgTypeInt, orgContracts, builder);
    }

    private ListContractReply processDate(List<Long> orgIds, Integer orgType, List<OrgContract> contracts, ListContractReply.Builder builder) {
        if (!CollectionUtils.isEmpty(contracts)) {
            List<ContractDetail> details = new ArrayList<>();
            List<Long> contractIds = contracts.stream().map(c -> c.getContractId()).collect(Collectors.toList());

            Map<Long, OrgContractDetail> contractDetailMap = null;
            if (!CollectionUtils.isEmpty(contractIds)) {
                Example example = new Example(OrgContractDetail.class);
                Example.Criteria criteria = example.createCriteria();
                criteria.andIn("orgId", orgIds);
                criteria.andEqualTo("orgType", orgType);
                criteria.andIn("contractId", contractIds);
                List<OrgContractDetail> contractDetails = orgContractDetailMapper.selectByExample(example);
                contractDetailMap = contractDetails.stream().collect(Collectors.toMap(OrgContractDetail::getContractId, Function.identity()));
            }

            for (OrgContract c : contracts) {
                ContractDetail.Builder detailBuilder = ContractDetail.newBuilder();
                BeanUtils.copyProperties(c, detailBuilder);
                if (null != contractDetailMap && contractDetailMap.get(c.getContractId()) != null) {
                    OrgContractDetail orgContractDetail = contractDetailMap.get(c.getContractId());
                    BeanUtils.copyProperties(orgContractDetail, detailBuilder);
                } else {
                    if (orgType == OrgContract.BROKER_ORG) {
                        Broker broker = brokerService.getBrokerByBrokerId(c.getBrokerId());
                        detailBuilder.setContractOrgName(broker.getName());
                    } else if (orgType == OrgContract.EXCHANGE_RG) {
                        ExchangeInfo exchangeInfo = exchangeInfoService.getExchangeInfoById(c.getExchangeId());
                        detailBuilder.setContractOrgName(exchangeInfo.getExchangeName());
                    }
                }
                if (orgType == OrgContract.BROKER_ORG) {
                    detailBuilder.setStatus(c.getBrokerStatus());
                } else if (orgType == OrgContract.EXCHANGE_RG) {
                    detailBuilder.setStatus(c.getExchangeStatus());
                }
                details.add(detailBuilder.build());
            }
            builder.addAllContractDetail(details);
        }
        return builder.build();
    }

    @Override
    public Boolean addApplication(AddContractRequest request) {
        Broker brokerById = brokerService.getBrokerByBrokerId(request.getBrokerId());
        ExchangeInfo exchangeInfoById = exchangeInfoService.getExchangeInfoByExchangeId(request.getExchangeId());
        if (null == brokerById || null == exchangeInfoById) {
            return false;
        }
        OrgContract contract = new OrgContract();
        OrgContractDetail brokerContractDetail = new OrgContractDetail();
        OrgContractDetail exchangeContractDetail = new OrgContractDetail();
        // contract
        BeanUtils.copyProperties(request, contract);
        if (OrgType.Broker_Org.equals(request.getApplyOrgType())) {
            contract.setBrokerStatus(OrgContract.OTHER_CONFIRMED_STATUS);
            contract.setExchangeStatus(OrgContract.APPLY_STATUS);
            // contract detail
            BeanUtils.copyProperties(request, brokerContractDetail);
            exchangeContractDetail.setContractOrgName(brokerById.getName());
        } else if (OrgType.Exchange_Org.equals(request.getApplyOrgType())) {
            contract.setBrokerStatus(OrgContract.APPLY_STATUS);
            contract.setExchangeStatus(OrgContract.OTHER_CONFIRMED_STATUS);
            // contract detail
            BeanUtils.copyProperties(request, exchangeContractDetail);
            brokerContractDetail.setContractOrgName(exchangeInfoById.getExchangeName());
        }
        contract.setCreatedAt(System.currentTimeMillis());
        // broker detail
        brokerContractDetail.setContractId(request.getContractId());
        brokerContractDetail.setOrgId(request.getBrokerId());
        brokerContractDetail.setOrgType(OrgContract.BROKER_ORG);
        brokerContractDetail.setCreatedAt(System.currentTimeMillis());
        // exchange detail
        exchangeContractDetail.setContractId(request.getContractId());
        exchangeContractDetail.setOrgId(request.getExchangeId());
        exchangeContractDetail.setOrgType(OrgContract.EXCHANGE_RG);
        exchangeContractDetail.setCreatedAt(System.currentTimeMillis());

        orgContractDetailMapper.insert(brokerContractDetail);
        orgContractDetailMapper.insert(exchangeContractDetail);

        return orgContractMapper.insert(contract) > 0? true: false;
    }

    @Override
    public Boolean editContactInfo(UpdateContactInfoRequest request) {
        Example example = new Example(OrgContractDetail.class);
        Example.Criteria criteria = example.createCriteria();
        if (OrgType.Broker_Org.equals(request.getApplyOrgType())) {
            criteria.andEqualTo("orgType", OrgContract.BROKER_ORG);
        } else if (OrgType.Exchange_Org.equals(request.getApplyOrgType())) {
            criteria.andEqualTo("orgType", OrgContract.EXCHANGE_RG);
        }
        criteria.andEqualTo("orgId", request.getOrgId());
        criteria.andEqualTo("contractId", request.getContractId());
        OrgContractDetail oldContract = orgContractDetailMapper.selectOneByExample(example);
        if (null != oldContract) {
            oldContract.setContact(request.getContact());
            oldContract.setEmail(request.getEmail());
            oldContract.setPhone(request.getPhone());
            oldContract.setContractOrgName(request.getContractOrgName());
            oldContract.setCompanyName(request.getCompanyName());
            oldContract.setRemark(request.getRemark());
            oldContract.setCreatedAt(System.currentTimeMillis());
            return orgContractDetailMapper.updateByPrimaryKey(oldContract) > 0? true: false;
        } else {
            return false;
        }
    }

    @Override
    public Boolean enableApplication(Long orgId, Long contractId, OrgType applyOrgType) {
        log.info(String.format("brokerId: %s; contractId: %s", orgId, contractId));
        boolean isOk = false;
        OrgContract orgContract = null;
        if (OrgType.Broker_Org.equals(applyOrgType)) {
            orgContract = orgContractMapper.getBrokerContract(orgId, contractId);
            if (null != orgContract && orgContract.getBrokerStatus() == OrgContract.APPLY_STATUS) {
                isOk = orgContractMapper.updateBrokerContractStatus(orgId, contractId, OrgContract.ALLOW_STATUS, OrgContract.ALLOW_STATUS) > 0 ? true : false;
            }
        } else if (OrgType.Exchange_Org.equals(applyOrgType)) {
            orgContract = orgContractMapper.getExchangeContract(orgId, contractId);
            if (null != orgContract && orgContract.getExchangeStatus() == OrgContract.APPLY_STATUS) {
                isOk = orgContractMapper.updateExchangeContractStatus(orgId, contractId, OrgContract.ALLOW_STATUS, OrgContract.ALLOW_STATUS) > 0 ? true : false;
            }
        }
        if (isOk) {
            enableContract(orgContract.getBrokerId(), orgContract.getExchangeId(), Boolean.TRUE, true);
        }
        return isOk;
    }

    @Override
    public Boolean rejectApplication(Long orgId, Long contractId, OrgType applyOrgType) {
        log.info(String.format("brokerId: %s; contractId: %s",orgId, contractId));
        boolean isOk = false;

        if (OrgType.Broker_Org.equals(applyOrgType)) {
            OrgContract brokerContract = orgContractMapper.getBrokerContract(orgId, contractId);
            if (null != brokerContract && brokerContract.getBrokerStatus() == OrgContract.APPLY_STATUS) {
                isOk = orgContractMapper.updateBrokerContractStatus(orgId, contractId, OrgContract.REJECTED_STATUS, OrgContract.REJECTED_STATUS) > 0 ? true : false;
            }
        } else if (OrgType.Exchange_Org.equals(applyOrgType)) {
            OrgContract exchangeContract = orgContractMapper.getExchangeContract(orgId, contractId);
            if (null != exchangeContract && exchangeContract.getExchangeStatus() == OrgContract.APPLY_STATUS) {
                isOk = orgContractMapper.updateExchangeContractStatus(orgId, contractId, OrgContract.REJECTED_STATUS, OrgContract.REJECTED_STATUS) > 0 ? true : false;
            }
        }
        return isOk;
    }

    @Override
    public Boolean closeOrgContract(Long orgId, Long contractId, OrgType applyOrgType) {
        log.info(String.format("brokerId: %s; contractId: %s",orgId, contractId));
        boolean isOk = false;
        OrgContract orgContract = null;
        if (OrgType.Broker_Org.equals(applyOrgType)) {
            orgContract = orgContractMapper.getBrokerContract(orgId, contractId);
            if (null != orgContract && orgContract.getBrokerStatus() == OrgContract.ALLOW_STATUS) {
                isOk = orgContractMapper.updateBrokerContractStatus(orgId, contractId, OrgContract.CLOSED_STATUS, OrgContract.CLOSED_STATUS) > 0 ? true : false;
            }
        } else if (OrgType.Exchange_Org.equals(applyOrgType)) {
            orgContract = orgContractMapper.getExchangeContract(orgId, contractId);
            if (null != orgContract && orgContract.getExchangeStatus() == OrgContract.ALLOW_STATUS) {
                isOk = orgContractMapper.updateExchangeContractStatus(orgId, contractId, OrgContract.CLOSED_STATUS, OrgContract.CLOSED_STATUS) > 0 ? true : false;
            }
        }
        if (isOk) {
            enableContract(orgContract.getBrokerId(), orgContract.getExchangeId(), Boolean.FALSE, true);
        }
        return isOk;
    }

    @Override
    public Boolean reopenOrgContract(Long orgId, Long contractId, OrgType applyOrgType) {
        log.info(String.format("brokerId: %s; contractId: %s", orgId, contractId));
        boolean isOk = false;

        if (OrgType.Broker_Org.equals(applyOrgType)) {
            OrgContract brokerContract = orgContractMapper.getBrokerContract(orgId, contractId);
            if (null != brokerContract && (brokerContract.getBrokerStatus() == OrgContract.REJECTED_STATUS ||  brokerContract.getBrokerStatus() == OrgContract.CLOSED_STATUS)) {
                isOk = orgContractMapper.updateBrokerContractStatus(orgId, contractId, OrgContract.APPLY_STATUS, OrgContract.REOPEN_STATUS) > 0 ? true : false;
            }
        } else if (OrgType.Exchange_Org.equals(applyOrgType)) {
            OrgContract exchangeContract = orgContractMapper.getExchangeContract(orgId, contractId);
            if (null != exchangeContract && (exchangeContract.getExchangeStatus() == OrgContract.REJECTED_STATUS ||  exchangeContract.getExchangeStatus() == OrgContract.CLOSED_STATUS)) {
                isOk = orgContractMapper.updateExchangeContractStatus(orgId, contractId, OrgContract.REOPEN_STATUS, OrgContract.APPLY_STATUS) > 0 ? true : false;
            }
        }
        return isOk;
    }

    @Override
    public ListContractReply listAllOrgContractInfo(List<Long> orgIds, OrgType orgType) {
        Integer orgTypeInt = 0;
        List<OrgContract> orgContracts = null;
        Example example = new Example(OrgContract.class);
        Example.Criteria criteria = example.createCriteria();
        if (orgType.equals(OrgType.Broker_Org)) {
            orgTypeInt = OrgContract.BROKER_ORG;
            criteria.andIn("brokerId", orgIds);
            criteria.andEqualTo("brokerStatus", OrgContract.ALLOW_STATUS);
        } else if (orgType.equals(OrgType.Exchange_Org)) {
            orgTypeInt = OrgContract.EXCHANGE_RG;
            criteria.andIn("exchangeId", orgIds);
            criteria.andEqualTo("exchangeStatus", OrgContract.ALLOW_STATUS);
        }
        orgContracts = orgContractMapper.selectByExample(example);

        return processDate(orgIds, orgTypeInt, orgContracts, ListContractReply.newBuilder());
    }

    private Boolean enableContract(Long brokerId, Long exchangeId, Boolean enable, Boolean trust) {
        try {
            ConfigBrokerExchangeContractRequest request = ConfigBrokerExchangeContractRequest.newBuilder()
                    .setBrokerId(brokerId)
                    .setExchangeId(exchangeId)
                    .setEnableContract(enable)
                    .setIsTrust(trust)
                    .build();
            bhOrgClient.configBrokerExchangeContract(request);
            return true;
        } catch (Exception e) {
            log.info("Saas Create Org Contract: Call BH Error.", e);
            return false;
        }
    }

    @Override
    public OrgContract getOrgContractInContract(Long exchangeId, Long brokerId) {
        return orgContractMapper.getOrgContractInContract(exchangeId, brokerId);
    }

    @Override
    public Boolean editOrgContract(long exchangeId, long brokerId, long applyOrgId, boolean open, boolean trust) {
        Example example = new Example(OrgContract.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("exchangeId", exchangeId);
        criteria.andEqualTo("brokerId", brokerId);
        OrgContract contract = orgContractMapper.selectOneByExample(example);

        if (contract == null) {
            contract = new OrgContract();
            contract.setContractId(idGenerator.nextId());
            contract.setBrokerId(brokerId);
            contract.setExchangeId(exchangeId);
            contract.setBrokerStatus(OrgContract.ALLOW_STATUS);
            contract.setExchangeStatus(OrgContract.ALLOW_STATUS);
            contract.setCreatedAt(System.currentTimeMillis());
            contract.setApplyOrgId(applyOrgId);
            orgContractMapper.insertSelective(contract);
        } else {
            contract.setApplyOrgId(applyOrgId);
            contract.setBrokerStatus(open ? OrgContract.ALLOW_STATUS : OrgContract.CLOSED_STATUS);
            contract.setExchangeStatus(open ? OrgContract.ALLOW_STATUS : OrgContract.CLOSED_STATUS);
            orgContractMapper.updateByPrimaryKey(contract);
        }
        enableContract(brokerId, exchangeId, open, trust);
        return true;
    }
}
