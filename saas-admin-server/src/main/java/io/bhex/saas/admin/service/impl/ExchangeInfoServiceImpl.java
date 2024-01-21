package io.bhex.saas.admin.service.impl;

import io.bhex.base.account.*;
import io.bhex.base.proto.DecimalUtil;
import io.bhex.bhop.common.dto.param.CreateAdminUserPO;
import io.bhex.bhop.common.dto.param.CreateUserType;
import io.bhex.bhop.common.exception.BizException;
import io.bhex.bhop.common.exception.ErrorCode;
import io.bhex.bhop.common.util.Combo2;
import io.bhex.bhop.common.util.LocaleUtil;
import io.bhex.bhop.common.util.MD5Util;
import io.bhex.saas.admin.config.FeignConfig;
import io.bhex.saas.admin.controller.dto.EditExchangePO;
import io.bhex.saas.admin.dao.ExchangeInfoMapper;
import io.bhex.saas.admin.dao.ExchangeInstanceDetailMapper;
import io.bhex.saas.admin.dao.ExchangeInstanceMapper;
import io.bhex.saas.admin.grpc.client.BhOrgClient;
import io.bhex.saas.admin.grpc.client.FeeClient;
import io.bhex.saas.admin.grpc.client.IQuoteClient;
import io.bhex.saas.admin.http.param.NewExchangePO;
import io.bhex.saas.admin.http.response.ExchangeResultRes;
import io.bhex.saas.admin.model.ExchangeInfo;
import io.bhex.saas.admin.model.ExchangeInstance;
import io.bhex.saas.admin.model.ExchangeInstanceDetail;
import io.bhex.saas.admin.service.ExchangeInfoService;
import io.bhex.saas.admin.service.SaasFeeSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class ExchangeInfoServiceImpl implements ExchangeInfoService {

    @Autowired
    private ExchangeInstanceMapper exchangeInstanceMapper;
    @Autowired
    private ExchangeInfoMapper exchangeInfoMapper;
    @Autowired
    private ExchangeInstanceDetailMapper exchangeInstanceDetailMapper;
    @Autowired
    private FeeClient feeClient;
    @Autowired
    private BhOrgClient bhOrgClient;
    @Autowired
    private SaasFeeSettingService saasFeeSettingService;
    @Autowired
    private IQuoteClient quoteClient;

    private Long createOrgInBhPlatfrom(ExchangeInfo exchangeInfo){
        Long orgId = exchangeInfoMapper.getMaxId() + 1;

        for(int i=0; i<10; i++){
            OrgRegisterReply.Result result = bhOrgClient.orgRegisterExchange(exchangeInfo.getExchangeName(), exchangeInfo.getCompany(),1L, orgId).getResult();
            log.info("add org:{} {}", orgId, result);
            boolean suc = result.equals(OrgRegisterReply.Result.OK) || result.equals(OrgRegisterReply.Result.ORG_EXISTED);
            if(suc){
                return orgId;
            }
            orgId++;
        }
        throw new BizException(ErrorCode.ERROR, "create exchange failed");
    }


    @Override
    public List<ExchangeInfo> getExchangeInfoList(List<Long> exchangeIdList) {
        Example example = new Example(ExchangeInfo.class);
        example.createCriteria()
            .andIn("exchangeId", exchangeIdList);
        return exchangeInfoMapper.selectByExample(example);
    }

    @Override
    @Transactional
    public Combo2<Boolean, String> addExchangeInfo(Long instanceId, ExchangeInfo exchangeInfo, BigDecimal saasFee, Integer category) {
        if (getExchangeInfoByExchangeName(exchangeInfo.getExchangeName()) != null) {
            return new Combo2<>(false, exchangeInfo.getExchangeName() + " has existed");
        }

        if (exchangeInfoMapper.countByEmail(exchangeInfo.getEmail()) > 0) {
            return new Combo2<>(false, exchangeInfo.getEmail() + " has existed");
        }

        long now = System.currentTimeMillis();
        Long orgId = createOrgInBhPlatfrom(exchangeInfo);
        exchangeInfo.setId(orgId);
        exchangeInfo.setExchangeId(orgId);
        exchangeInfo.setSaltStr(MD5Util.getMD5(now + "").toUpperCase().substring(0, 6));
        exchangeInfoMapper.insert(exchangeInfo);
        log.info("add exchangeinfo success id:{}", exchangeInfo.getId());

        //insert saas fee rate record
//        boolean createSaasFeeSuc = saasFeeSettingService.createExchangeSaasFeeSetting(brokerId, exchangeInfo.getSaasFeeRate());
//        if(!createSaasFeeSuc){
//            throw new BizException(ErrorCode.ERROR, "fail insert saas fee rate");
//        }

        ExchangeInstance instance = exchangeInstanceMapper.selectByPrimaryKey(instanceId);

        //no2. call ex-gateway to create a new exchange with (exchange_id,instance_id)
        NewExchangePO param = NewExchangePO.builder()
                .exchangeId(orgId)
                .exchangeName(exchangeInfo.getExchangeName())
                .remark("new exchange")
                .build();
        ExchangeResultRes<Object> result = FeignConfig.getExchangeGatewayClient(instance.getGatewayUrl()).newExchange(param);
        log.info("new exchange result : {}", result);

        if (result.getStatus() != 200 && result.getStatus() != 1302) {
            //创建成功或者已存在
            log.error("call ex-gateway error : ", result.getErr());
            throw new BizException(ErrorCode.ERROR, "call ex-gateway error " + result.getErr());
            //return new Combo2<>(false, result.getErr());
        }

        quoteClient.createResourcesWithNewExchange(orgId);

        ExchangeInstanceDetail detail = new ExchangeInstanceDetail();
        detail.setId(orgId);
        detail.setInstanceId(instanceId);
        detail.setClusterName(instance.getClusterName());
        detail.setInstanceName(instance.getInstanceName());
        detail.setGatewayUrl(instance.getGatewayUrl());
        String adminDomain = String.format(instance.getAdminWebDomainPattern(), String.valueOf(orgId),  MD5Util.getMD5(exchangeInfo.getSaltStr()  + orgId).toLowerCase().substring(0, 8));
        detail.setAdminWebUrl(instance.getAdminWebProtocal() + "://" + adminDomain + "/");
        detail.setAdminInternalApiUrl(instance.getAdminInternalApiUrl());
        detail.setAdminWebDomain(adminDomain);
        detail.setStatus(0);
        detail.setExchangeId(orgId);
        detail.setExchangeName(exchangeInfo.getExchangeName());
        detail.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        detail.setDeleted(0);
        detail.setForbidAccess(0);
        exchangeInstanceDetailMapper.insertSelective(detail);

        createExchangeAdmin(exchangeInfo);

        updateSaasFee(orgId, saasFee, category);

        return new Combo2<>(true, "create successfully");
    }

    private void updateSaasFee(Long exchangeId, BigDecimal saasFee, Integer category) {
        if (saasFee.compareTo(BigDecimal.ZERO) < 0 || saasFee.compareTo(BigDecimal.ONE) > 0) {
            throw new BizException(ErrorCode.SAAS_FEE_OVER_LIMIT);
        }
        // todo: saas fee default
        UpdateSaasCommissionFeeResponse response = feeClient.updateSaasCommissionFee(UpdateSaasCommissionFeeRequest.newBuilder()
                .setExchangeId(exchangeId)
                .setCommissionType(category)
                .setCommissionRate(DecimalUtil.fromBigDecimal(saasFee))
                .build()
        );
        if (response.getErrCode() != 200) {
            log.error("updateSaasFee Error: error code=>{}, CommissionRate => {}, exchangeId => {}", response.getErrCode(), saasFee.toPlainString(), exchangeId);
        }
    }

    private void createExchangeAdmin(ExchangeInfo exchangeInfo) {
        CreateAdminUserPO userPO = new CreateAdminUserPO();
        userPO.setOrgId(exchangeInfo.getExchangeId());
        userPO.setOrgName(exchangeInfo.getExchangeName());
        userPO.setSaasOrgId(exchangeInfo.getId());
        userPO.setTelephone(exchangeInfo.getContactTelephone());
        userPO.setUsername(exchangeInfo.getEmail());
        userPO.setEmail(exchangeInfo.getEmail());
        userPO.setDefaultLanguage(LocaleUtil.getLanguage());
        userPO.setCreateUserType(CreateUserType.EXCHANGE_ADMIN);
        ExchangeInstanceDetail instanceDetail = exchangeInstanceDetailMapper.getInstanceDetailByExchangeId(exchangeInfo.getExchangeId());
        userPO.setAdminWebUrl(instanceDetail.getAdminWebUrl());

//        ExchangeAdminHttpClient exchangeAdminClient = FeignConfig.getExchangeAdminClient(instanceDetail.getAdminInternalApiUrl());
//        AdminResultRes<Object> adminResultBean = exchangeAdminClient.createUser(userPO);
//        log.info("ex-admin create user result = {}", adminResultBean);
//        if (adminResultBean.getCode() != 0) {
//            log.error("ex-admin create user error , {}", adminResultBean.getMsg());
//            throw new BizException(ErrorCode.ERROR, "ex-admin create user error " + adminResultBean.getMsg());
//            //return new Combo2<>(false, adminResultBean.getMsg());
//        }
    }

    @Override
    public ExchangeInfo getExchangeInfoByExchangeName(String exchangeName) {
        return exchangeInfoMapper.getExchangeInfoByExchangeName(exchangeName);
    }

    @Override
    public ExchangeInfo getExchangeInfoById(Long id) {
        ExchangeInfo info = exchangeInfoMapper.getExchangeInfoById(id);
        if (info == null) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "exchange saasid wrong " + id);
        }
        //ExchangeSaasFeeRate setting = saasFeeSettingService.getLatestExchangeSaasFeeSetting(info.getExchangeId());
        //info.setSaasFeeRate(setting == null ? BigDecimal.ZERO : setting.getFeeRate());
        return info;
    }

    @Override
    public ExchangeInfo getExchangeInfoByExchangeId(Long id) {
        ExchangeInfo info = exchangeInfoMapper.getExchangeInfoByExchangeId(id);
        if (info == null) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "exchange saasid wrong " + id);
        }
        return info;
    }

    @Override
    @Transactional
    public boolean updateExchangeStatus(Long id, int newStatus) {
        ExchangeInfo info = getExchangeInfoById(id);
        if (info == null) {
            return false;
        }
        if (info.getStatus() == newStatus) {
            return false;
        }

        //enable or disable in bluehexlix platform
        bhOrgClient.orgSwitch(info.getExchangeId(), newStatus == 1 ? true : false);

        return exchangeInfoMapper.updateStatus(id, info.getStatus(), newStatus, System.currentTimeMillis()) == 1;
    }

    @Override
    @Transactional
    public boolean forbidAccess(Long exchangeId) {
        ExchangeInstanceDetail instanceDetail = exchangeInstanceDetailMapper.getInstanceDetailByExchangeId(exchangeId);
        if (instanceDetail != null) {
            instanceDetail.setForbidAccess(1);
            int row = exchangeInstanceDetailMapper.updateByPrimaryKey(instanceDetail);
            if (row != 1) {
                throw new BizException(ErrorCode.ERROR);
            }
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean editExchangeInfo(EditExchangePO po, Integer category) {

        ExchangeInfo info = exchangeInfoMapper.getExchangeInfoById(po.getId());
        if (info == null) {
            throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "exchange saasid wrong " + po.getId());
        }

        if (!info.getExchangeName().equals(po.getExchangeName())) {
            //变更orgname
            info.setExchangeName(po.getExchangeName());
            ChangeOrgNameReply reply = bhOrgClient.changeOrgName(info.getExchangeId(), po.getExchangeName());
            if (reply.getCode() != 0) {
                throw new BizException(ErrorCode.REQUEST_PARAMETER_VALIDATE_FAIL, "change org name error" + reply.getMessage());
            }

            ExchangeInstanceDetail instanceDetail = getInstanceInfoByExchangeId(info.getExchangeId());
            instanceDetail.setExchangeName(po.getExchangeName());
            exchangeInstanceDetailMapper.updateByPrimaryKeySelective(instanceDetail);
        }

//        if (!info.getEmail().equals(po.getEmail())) {
//            //变更 admin 邮箱
//
//            ChangeAdminUserPO changeAdminUserPO = new ChangeAdminUserPO();
//            changeAdminUserPO.setOrgId(info.getExchangeId());
//            changeAdminUserPO.setNewEmail(po.getEmail());
//            changeAdminUserPO.setEmail(info.getEmail());
//            ExchangeInstanceDetail instanceDetail = getInstanceInfoByExchangeId(info.getExchangeId());
//            AdminResultRes<Object> result = FeignConfig.getExchangeAdminClient(instanceDetail.getAdminInternalApiUrl())
//                    .changeAdminUser(changeAdminUserPO);
//            log.info("change admin email : {}", result);
//            if (result.getCode() != 0) {
//                throw new BizException(ErrorCode.ERR_REQUEST_PARAMETER, "change admin email error " + po.getEmail());
//            }
//            instanceDetail.setExchangeName(po.getExchangeName());
//            exchangeInstanceDetailMapper.updateByPrimaryKeySelective(instanceDetail);
//
//            info.setEmail(po.getEmail());
//        }


        info.setCompany(po.getCompany());
        info.setContactName(po.getContactName());
        info.setContactTelephone(po.getContactTelephone());
        info.setRemark(po.getRemark());
        int num = exchangeInfoMapper.updateByPrimaryKey(info);



        log.error("editExchangeInfo : exchangeId {}, saasFee {}", info.getExchangeId(), po.getSaasFee());
        updateSaasFee(info.getExchangeId(), po.getSaasFee(), category);

        return num == 1;
    }

    @Override
    public List<ExchangeInfo> queryAllExchanges() {
        return exchangeInfoMapper.getAllExchanges();
    }

    @Override
    public Combo2<List<ExchangeInfo>, Integer> queryExchangeInfos(int current, int pageSize, String exchangeName, Long exchangeId) {
        int total = exchangeInfoMapper.countExchanges(exchangeName, exchangeId);
        int start = (current - 1) * pageSize;
        if (start >= total) {
            return new Combo2<>(new ArrayList<>(), total);
        }
        List<ExchangeInfo> infos = exchangeInfoMapper.queryExchanges(start, pageSize, exchangeName, exchangeId);

        return new Combo2<>(infos, total);
    }

    public ExchangeInstanceDetail getInstanceInfoByExchangeId(Long exchangeId) {
        ExchangeInstanceDetail instance = exchangeInstanceDetailMapper.getInstanceDetailByExchangeId(exchangeId);
        if (instance.getStatus() != 1) {
            return instance;
        }

        //到exchangeadmin检验密码是否设置成功
//        ExchangeAdminHttpClient exchangeAdminClient = FeignConfig.getExchangeAdminClient(instance.getAdminInternalApiUrl());
//        OrgIdPO orgIdPO = new OrgIdPO();
//        orgIdPO.setOrgId(exchangeId);
//        AdminResultRes<Boolean> res = exchangeAdminClient.hasSetPasswordOk(orgIdPO);
//        if (res.getCode() == 0 && res.getData()) {
//            exchangeInstanceDetailMapper.updateStatus(exchangeId, 2);
//            instance.setStatus(2);
//        }

        return instance;
    }

    @Override
    public BigDecimal getSaasFee(Long exchangeId, Integer category) {
        return DecimalUtil.toBigDecimal(feeClient.getSaasCommissionFee(GetSaasCommissionFeeRequest.newBuilder().setExchangeId(exchangeId).setCommissionType(category).build()).getCommissionRate());
    }

    public boolean updateExchangeInstanceStatus(Long exchangeId, Integer status) {
        return exchangeInstanceDetailMapper.updateStatus(exchangeId, status) == 1;
    }

    public boolean sendSetPasswordEmail(Long exchangeId) {
//        ExchangeInstanceDetail instance = exchangeInstanceDetailMapper.getInstanceDetailByExchangeId(exchangeId);
//        ExchangeAdminHttpClient exchangeAdminClient = FeignConfig.getExchangeAdminClient(instance.getAdminInternalApiUrl());
//        SetPasswordEmailPO po = new SetPasswordEmailPO();
//        po.setAdminWebUrl(instance.getAdminWebUrl());
//        po.setOrgId(exchangeId);
//        AdminResultRes<Boolean> res = exchangeAdminClient.sendSetPasswordEmail(po);
//        return res.getData();
        return false;
    }

}
