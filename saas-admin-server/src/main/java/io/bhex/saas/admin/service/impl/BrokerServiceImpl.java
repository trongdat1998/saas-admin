package io.bhex.saas.admin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.bhex.base.account.*;
import io.bhex.bhop.common.dto.PaginationVO;
import io.bhex.bhop.common.dto.param.CreateAdminUserPO;
import io.bhex.bhop.common.dto.param.CreateUserType;
import io.bhex.bhop.common.dto.param.OrgIdPO;
import io.bhex.bhop.common.dto.param.SetPasswordEmailPO;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.LocaleUtil;
import io.bhex.bhop.common.util.MD5Util;
import io.bhex.bhop.common.util.PageUtil;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.constants.BizConstant;
import io.bhex.saas.admin.controller.dto.BrokerInfoRes;
import io.bhex.saas.admin.controller.dto.EditBrokerPO;
import io.bhex.saas.admin.controller.dto.SetTransferWhitelistPO;
import io.bhex.saas.admin.controller.dto.VerifyFlowRecordDTO;
import io.bhex.saas.admin.dao.BrokerInstanceDetailMapper;
import io.bhex.saas.admin.dao.BrokerInstanceMapper;
import io.bhex.saas.admin.dao.BrokerMapper;
import io.bhex.saas.admin.enums.RegisterOptionType;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.http.BrokerHttpClient;
import io.bhex.saas.admin.http.param.EnableBrokerPO;
import io.bhex.saas.admin.http.response.AdminResultRes;
import io.bhex.saas.admin.model.Broker;
import io.bhex.saas.admin.model.BrokerInstance;
import io.bhex.saas.admin.model.BrokerInstanceDetail;
import io.bhex.saas.admin.model.VerifyBizRecord;
import io.bhex.saas.admin.service.BrokerService;
import io.bhex.saas.admin.service.CommonIniService;
import io.bhex.saas.admin.service.SmsSignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 15/08/2018 11:47 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class BrokerServiceImpl implements BrokerService {

    @Autowired
    private BrokerMapper brokerMapper;

    @Autowired
    private BhOrgClient bhOrgClient;

    @Autowired
    private VerifyFlowService verifyFlowService;
    @Autowired
    private BrokerInstanceMapper brokerInstanceMapper;
    @Autowired
    private BrokerInstanceDetailMapper brokerInstanceDetailMapper;
    @Autowired
    private SmsSignService smsSignService;
    @Autowired
    private CommonIniService commonIniService;

    private static final Cache<Long, String> BROKER_NAME_CACHE =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    @Override
    public List<Broker> queryAllBrokers() {
        return brokerMapper.getAllBrokers();
    }

    @Override
    public Map<Long, String> queryBrokerName() {
        Map<Long, String> brokerNameCache = Maps.newHashMap();
        List<Broker> brokers = brokerMapper.getAllBrokers();
        if (CollectionUtils.isEmpty(brokers)) {
            return Maps.newHashMap();
        }
        brokers.forEach(b -> brokerNameCache.put(b.getBrokerId(), b.getName()));
        return brokerNameCache;
    }

    @Override
    public PaginationVO<BrokerInfoRes> queryBroker(int current, int pageSize, String brokerName, Long brokerId) {
        Integer total = brokerMapper.countBroker(brokerName, brokerId);
        PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);

        List<VerifyFlowRecordDTO> verifyFlowRecords = verifyFlowService.queryAllRecords(0, BizConstant.CHANGE_BROKER_BIZ_TYPE,
                Lists.newArrayList(VerifyBizRecord.VERIFING_STATUS), 0, 100, 0L);
        if (CollectionUtils.isEmpty(verifyFlowRecords)) {
            verifyFlowRecords = Lists.newArrayList();
        }
        List<Long> verifyingList = verifyFlowRecords.stream().map(v -> {
            EditBrokerPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(v.getVerifyContent()), EditBrokerPO.class);
            return verifyPO.getBrokerId();
        }).collect(Collectors.toList());

        String otcShareListStr = FeignConfig.getOtcClient().listShareBrokers();
        List<String> otcShareList = otcShareListStr != null ? Lists.newArrayList(otcShareListStr.split(",")) : Lists.newArrayList();

        List<Broker> brokers = brokerMapper.queryBroker(page.getStart(), page.getOffset(), brokerName, brokerId);
        if (!CollectionUtils.isEmpty(brokers)) {
            List<BrokerInfoRes> infoList = new ArrayList<>();
            PaginationVO<BrokerInfoRes> vo = new PaginationVO();
            for (Broker b: brokers) {
                BrokerInfoRes res = new BrokerInfoRes();
                BeanUtils.copyProperties(b, res);
                BrokerInstanceDetail detail = getInstanceInfoByBrokerId(b.getBrokerId());
                res.setDnsSetting(detail.getStatus());
                res.setAdminWebUrl(detail.getAdminWebUrl());
                res.setCreatedAt(detail.getCreatedAt().getTime());
                res.setForbidAccessStatus(detail.getForbidAccess() == 1);
                res.setOtcShare(otcShareList.contains(b.getBrokerId().toString()));
                res.setDueTime(detail.getDueTime());
                res.setVerifying(verifyingList.contains(b.getBrokerId()));
                infoList.add(res);
            }
            vo.setCurrent(current);
            vo.setPageSize(pageSize);
            vo.setTotal(total);
            vo.setList(infoList);
            return vo;
        }
        return null;
    }

    @Override
    public PaginationVO<BrokerInfoRes> queryBrokerTransferWhitelist(int current, int pageSize, String brokerName, Long brokerId) {
        Integer total = brokerMapper.countBroker(brokerName, brokerId);
        PageUtil.Page page = PageUtil.pageCount(current, pageSize, total);

        List<VerifyFlowRecordDTO> verifyFlowRecords = verifyFlowService.queryAllRecords(0, BizConstant.SAAS_TRANSFER_BIZ_TYPE,
                Lists.newArrayList(VerifyBizRecord.VERIFING_STATUS), 0, 100, 0L);
        if (CollectionUtils.isEmpty(verifyFlowRecords)) {
            verifyFlowRecords = Lists.newArrayList();
        }
        Set<Long> verifyingList = verifyFlowRecords.stream().map(v -> {
            SetTransferWhitelistPO verifyPO = JSONObject.parseObject(JSONObject.toJSONString(v.getVerifyContent()), SetTransferWhitelistPO.class);
            return verifyPO.getBrokerId();
        }).collect(Collectors.toSet());

        List<Broker> brokers = brokerMapper.queryBroker(page.getStart(), page.getOffset(), brokerName, brokerId);
        if (!CollectionUtils.isEmpty(brokers)) {
            List<BrokerInfoRes> infoList = new ArrayList<>();
            PaginationVO<BrokerInfoRes> vo = new PaginationVO();
            for (Broker b: brokers) {
                BrokerInfoRes res = new BrokerInfoRes();
                BeanUtils.copyProperties(b, res);
                BrokerInstanceDetail detail = getInstanceInfoByBrokerId(b.getBrokerId());
                res.setDnsSetting(detail.getStatus());
                res.setAdminWebUrl(detail.getAdminWebUrl());
                res.setCreatedAt(detail.getCreatedAt().getTime());
                res.setForbidAccessStatus(detail.getForbidAccess() == 1);
                res.setDueTime(detail.getDueTime());
                res.setVerifying(verifyingList.contains(b.getBrokerId()));
                infoList.add(res);
            }
            vo.setCurrent(current);
            vo.setPageSize(pageSize);
            vo.setTotal(total);
            vo.setList(infoList);
            return vo;
        }
        return null;
    }

    @Override
    public BrokerInfoRes queryBrokerDetail(Long brokerId) {
        Broker b = getBrokerByBrokerId(brokerId);
        BrokerInfoRes res = new BrokerInfoRes();
        BeanUtils.copyProperties(b, res);
        BrokerInstanceDetail detail = getInstanceInfoByBrokerId(b.getBrokerId());
        res.setDnsSetting(detail.getStatus());
        res.setAdminWebUrl(detail.getAdminWebUrl());
        res.setCreatedAt(detail.getCreatedAt().getTime());
        res.setForbidAccessStatus(detail.getForbidAccess() == 1);
        res.setDueTime(detail.getDueTime());
        return res;
    }

    @Override
    public Broker getBrokerById(Long id) {
        Broker broker = brokerMapper.selectByPrimaryKey(id);
        return broker == null ? getBrokerByBrokerId(id) : broker;
    }

    @Override
    public Broker getBrokerByBrokerId(Long brokerId) {
        return brokerMapper.getByBrokerId(brokerId);
    }

    @Override
    public Broker getBrokerByBrokerName(String brokerName) {
        return brokerMapper.getByBrokerName(brokerName);
    }

    private Long createOrgInBhPlatfrom(Broker broker) {
        long instanceId = broker.getInstanceId();
        Long orgId;
        if (instanceId == 2) { //创建bhex子站
            orgId = brokerMapper.getMaxBhexId() + 1;
        } else {
            orgId = brokerMapper.getMaxId() + 1;
        }

        for (int i = 0; i < 10; i++) {
            OrgRegisterRequest request = OrgRegisterRequest.newBuilder()
                    .setName(broker.getName())
                    .setFullName(broker.getCompany())
                    .setSaasId(1L)
                    .setGroupName("")
                    .setOrgId(orgId)
                    .setRole(OrgRole.BROKER_ROLE)
                    .build();
            OrgRegisterReply.Result result = bhOrgClient
                    .orgRegisterBroker(broker.getName(), broker.getCompany(), 1L, orgId).getResult();
            log.info("add org:{} {}", orgId, result);
            boolean suc = result.equals(OrgRegisterReply.Result.OK) || result.equals(OrgRegisterReply.Result.ORG_EXISTED);
            if (suc) {
                return orgId;
            }
            orgId++;
        }

        throw new BizException(ErrorCode.ERROR, "create broker failed");
    }

    @Override
    @Transactional
    public Long createBroker(Broker broker, long dueTime) {
        long now = System.currentTimeMillis();
        Long orgId = createOrgInBhPlatfrom(broker);

        broker.setBrokerId(orgId);
        broker.setId(orgId);
        broker.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        broker.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        broker.setEnabled(false);
        broker.setSaltStr(MD5Util.getMD5(now + "").toUpperCase().substring(0, 6));

        String random = MD5Util.getMD5(broker.getSaltStr() + orgId).toLowerCase().substring(0, 8);
        BrokerInstance instance = brokerInstanceMapper.getInstanceInfoById(broker.getInstanceId());
        BrokerInstanceDetail detail = new BrokerInstanceDetail();
        detail.setId(orgId);
        detail.setInstanceId(broker.getInstanceId());
        String apiDomain = broker.getApiDomain().charAt(0) == '.' ? broker.getApiDomain().substring(1) : broker.getApiDomain();
        String adminDomain = String.format(instance.getAdminWebDomainPattern(), String.valueOf(orgId), random, apiDomain);
        String adminDomainBhop = String.format(instance.getAdminWebDomainPattern(), String.valueOf(orgId), random, "bhop.cloud");
        detail.setAdminWebUrl(instance.getAdminWebProtocal() + "://" + adminDomainBhop + "/");
        detail.setAdminInternalApiUrl(instance.getAdminInternalApiUrl());
        detail.setAdminWebDomain(adminDomain + "," + adminDomainBhop);
        detail.setBrokerWebDomain(broker.getApiDomain());
        detail.setStatus(0);
        detail.setBrokerId(orgId);
        detail.setBrokerName(broker.getName());
        detail.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        detail.setDeleted(0);
        detail.setForbidAccess(0);
        detail.setFrontendCustomer(0);
        detail.setDueTime(dueTime);
        brokerInstanceDetailMapper.insertSelective(detail);

        createAdminUser(broker);
        brokerMapper.insertSelective(broker);
        return orgId;
    }

    private void createAdminUser(Broker broker) {
        CreateAdminUserPO userPO = new CreateAdminUserPO();
        userPO.setOrgId(broker.getBrokerId());
        userPO.setOrgName(broker.getName());
        userPO.setSaasOrgId(1L);
        userPO.setTelephone(broker.getPhone());
        userPO.setUsername(broker.getEmail());
        userPO.setEmail(broker.getEmail());
        userPO.setBrokerWebDomain(broker.getApiDomain());
        userPO.setDefaultLanguage(LocaleUtil.getLanguage());
        userPO.setCreateUserType(CreateUserType.BROKER_ADMIN);

        BrokerInstanceDetail instanceDetail = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(broker.getBrokerId());
        AdminResultRes<Object> result = FeignConfig.getBrokerClient(instanceDetail.getAdminInternalApiUrl()).createBroker(userPO);
        log.info("new broker result : {}", result);

        if (result.getCode() != 0) { //成功插入或者已经存在都认为成功
            log.error("call broker-http error : ", result.getMsg());
            throw new BizException(ErrorCode.ERROR, "call broker-http error " + result.getMsg());
        }
    }

    @Override
    public boolean updateBroker(EditBrokerPO po) {
        Broker broker = getBrokerById(po.getId());
        if (null == broker) {
            log.error("broker is not exist : ", po.getId());
            throw new BizException(ErrorCode.ERROR, "broker is not exist : " + po.getId());
        }
        broker.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        broker.setCompany(po.getCompany());
        broker.setContact(po.getContact());
        broker.setPhone(po.getPhone());
        broker.setBasicInfo(po.getBasicInfo());

        //saasFeeSettingService.updateBrokerSaasFeeSetting(broker.getBrokerId(), param.getSaasFeeRate());

        BrokerInstanceDetail instanceDetail = getInstanceInfoByBrokerId(broker.getBrokerId());
        if (po.getDueTime() > 0) {
            instanceDetail.setDueTime(po.getDueTime());
        }
        if (!broker.getName().equals(po.getName())) {
            //变更orgname
            ChangeOrgNameReply reply = bhOrgClient.changeOrgName(broker.getBrokerId(), po.getName());
            if (reply.getCode() != 0) {
                throw new BizException(ErrorCode.REQUEST_PARAMETER_VALIDATE_FAIL, "change org name error" + reply.getMessage());
            }

            io.bhex.saas.admin.model.SmsSign smsSign = smsSignService.getByOrgId(broker.getBrokerId());
            if (smsSign != null) {
                smsSignService.createSmsSign(broker.getBrokerId(), po.getName(), smsSign.getSign());
            }
        }

        if (!broker.getName().equals(po.getName()) || !broker.getApiDomain().equals(po.getApiDomain())) {
            po.setBrokerName(po.getName());
            po.setBrokerId(po.getBrokerId());
            AdminResultRes<Object> result = FeignConfig.getBrokerClient(instanceDetail.getAdminInternalApiUrl()).editBroker(po);
            if (result.getCode() != 0) {
                throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "change org name in brokerserver error " + po.getName());
            }

//            if (!broker.getApiDomain().equals(po.getApiDomain())) {
//                String oldDomain = broker.getApiDomain();
//                instanceDetail.setAdminWebDomain(instanceDetail.getAdminWebDomain().replaceAll(oldDomain, po.getApiDomain()));
//                instanceDetail.setAdminWebUrl(instanceDetail.getAdminWebUrl().replaceAll(oldDomain, po.getApiDomain()));
//            }

        }


//        if (!broker.getEmail().equals(po.getEmail())) {
//            //变更 admin 邮箱
//            ChangeAdminUserPO changeAdminUserPO = new ChangeAdminUserPO();
//            changeAdminUserPO.setOrgId(broker.getBrokerId());
//            changeAdminUserPO.setNewEmail(po.getEmail());
//            changeAdminUserPO.setEmail(broker.getEmail());
//
//            AdminResultRes<Object> result = FeignConfig.getBrokerClient(instanceDetail.getAdminInternalApiUrl())
//                    .changeAdminUser(changeAdminUserPO);
//            log.info("change admin email : {}", result);
//            if (result.getCode() != 0) {
//                throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "change admin email error " + po.getEmail());
//            }
//            broker.setEmail(po.getEmail());
//        }

        instanceDetail.setBrokerWebDomain(po.getApiDomain());
        instanceDetail.setBrokerName(po.getName());
        brokerInstanceDetailMapper.updateByPrimaryKeySelective(instanceDetail);

        broker.setApiDomain(po.getApiDomain());
        broker.setName(po.getName());
        return brokerMapper.updateByPrimaryKey(broker) > 0 ? true : false;
    }

    @Override
    @Transactional
    public boolean enableBroker(Long id, Boolean enabled) {
        //todo: plantform-server enable
        Broker broker = getBrokerById(id);
        if (null != broker) {
            broker.setEnabled(enabled);
            Boolean isOk = brokerMapper.updateByPrimaryKey(broker) > 0 ? true : false;
            if (isOk) {
                OrgSwitchReply orgSwitchReply = bhOrgClient.orgSwitch(broker.getBrokerId(), enabled);

                EnableBrokerPO enableBrokerPO = EnableBrokerPO.builder()
                        .brokerId(broker.getBrokerId())
                        .enabled(enabled)
                        .build();
                BrokerInstance instance = brokerInstanceMapper.getInstanceInfoById(broker.getInstanceId());
                AdminResultRes<Object> result = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).enableBroker(enableBrokerPO);
                log.info("new broker result : {}", result);

                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean forbidAccess(Long brokerId) {
        BrokerInstanceDetail instanceDetail = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
        if (instanceDetail != null) {
            instanceDetail.setForbidAccess(1);
            int row = brokerInstanceDetailMapper.updateByPrimaryKey(instanceDetail);
            if (row != 1) {
                throw new BizException(ErrorCode.ERROR);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean cancelforbidAccess(Long brokerId) {
        BrokerInstanceDetail instanceDetail = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
        if (instanceDetail != null) {
            instanceDetail.setForbidAccess(0);
            int row = brokerInstanceDetailMapper.updateByPrimaryKey(instanceDetail);
            if (row != 1) {
                throw new BizException(ErrorCode.ERROR);
            }
            return true;
        }
        return false;
    }

    @Override
    public String getEarnestAddress(Long id) {
        //todo: plantform-server getEarnestAddress
        Broker broker = getBrokerById(id);
        if (null != broker) {
            GetEarnestAddressReply earnestAddress = bhOrgClient.getEarnestAddress(broker.getBrokerId());
            return earnestAddress.getAddress();
        }
        return null;
    }

    @Override
    public BrokerInstanceDetail getInstanceInfoByBrokerId(Long brokerId) {
        BrokerInstanceDetail instance = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
        if (instance.getStatus() == 1) { //到exchangeadmin检验密码是否设置成功
            unknownStatusSet.add(brokerId);
        }
        return instance;
    }

    @Override
    public boolean updateBrokerInstanceStatus(Long brokerId, Integer status) {
        return brokerInstanceDetailMapper.updateStatus(brokerId, status) == 1;
    }

    @Override
    public boolean sendSetPasswordEmail(Long brokerId) {
        BrokerInstanceDetail instance = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
        SetPasswordEmailPO po = new SetPasswordEmailPO();
        po.setAdminWebUrl(instance.getAdminWebUrl());
        po.setOrgId(brokerId);
        AdminResultRes<Boolean> res = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl()).sendSetPasswordEmail(po);
        return res.getData();
    }

    @Override
    public boolean openOtcShare(Long id) {
        checkBroker(id);
        OrgIdPO orgIdPO = new OrgIdPO();
        orgIdPO.setOrgId(id);
        String result = FeignConfig.getOtcClient().addShareBroker(orgIdPO);
        return result.equalsIgnoreCase("success");
    }

    @Override
    public boolean cancelOtcShare(Long id) {
        checkBroker(id);
        OrgIdPO orgIdPO = new OrgIdPO();
        orgIdPO.setOrgId(id);
        String result = FeignConfig.getOtcClient().cancelShareBroker(orgIdPO);
        return result.equalsIgnoreCase("success");
    }

    private void checkBroker(Long id) {
       if (getBrokerByBrokerId(id) == null) {
           throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER);
       }
    }

    @Override
    public boolean updateBrokeDueTimer(long brokerId, long dueTime) {
        BrokerInstanceDetail instance = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
        instance.setDueTime(dueTime);
        return 1 == brokerInstanceDetailMapper.updateByPrimaryKeySelective(instance);
    }

    @Override
    public boolean updateBrokerRegisterOption(long id, RegisterOptionType type) {
        Broker broker = getBrokerById(id);
        if (null != broker && type != null) {
            broker.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            broker.setRegisterOption(type.getCode());
            return brokerMapper.updateByPrimaryKey(broker) > 0;
        }
        return false;
    }

    private static Set<Long> unknownStatusSet = new HashSet<>();

    @Scheduled(initialDelay = 3000, fixedRate = 240_000)
    public void checkUnknownInstanceStatus() {
        List<Long> brokers = unknownStatusSet.stream().collect(Collectors.toList());
        for (Long brokerId : brokers) {
            BrokerInstanceDetail instance = brokerInstanceDetailMapper.getInstanceDetailByBrokerId(brokerId);
            if (instance.getStatus() == 2) {
                continue;
            }
            BrokerHttpClient brokerClient = FeignConfig.getBrokerClient(instance.getAdminInternalApiUrl());
            OrgIdPO orgIdPO = new OrgIdPO();
            orgIdPO.setOrgId(brokerId);
            try {
                AdminResultRes<Boolean> res = brokerClient.hasSetPasswordOk(orgIdPO);
                if (res.getCode() == 0 && res.getData()) {
                    brokerInstanceDetailMapper.updateStatus(brokerId, 2);
                    instance.setStatus(2);
                    unknownStatusSet.remove(brokerId);
                }
            } catch (Exception e) {
                log.error("getBrokerStatus error {}", brokerId);
            }
        }
    }
}
