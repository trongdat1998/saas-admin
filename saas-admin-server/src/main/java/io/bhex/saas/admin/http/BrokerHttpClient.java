package io.bhex.saas.admin.http;

import feign.Headers;
import feign.RequestLine;
import io.bhex.bhop.common.dto.param.*;
import io.bhex.saas.admin.controller.dto.EditBrokerPO;
import io.bhex.saas.admin.controller.param.AddBrokerKycConfigPO;
import io.bhex.saas.admin.http.param.DeleteMarketAccountPO;
import io.bhex.saas.admin.http.param.EnableBrokerPO;
import io.bhex.saas.admin.http.param.BrokerIdPO;
import io.bhex.saas.admin.http.param.IdPO;
import io.bhex.saas.admin.http.param.QueryMarketAccountPO;
import io.bhex.saas.admin.http.param.SaveMarketAccountPO;
import io.bhex.saas.admin.http.param.SymbolMarketAccountPO;
import io.bhex.saas.admin.http.response.AdminResultRes;
import io.bhex.saas.admin.http.response.SymbolMarketAccountRes;

import java.util.List;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.http
 * @Author: ming.xu
 * @CreateDate: 17/08/2018 12:02 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface BrokerHttpClient {

    @RequestLine("POST /api/v1/user/create_user")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> createBroker(CreateAdminUserPO brokerCreatePO);

    @RequestLine("POST /api/v1/user/change_admin_user")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> changeAdminUser(ChangeAdminUserPO changeAdminUserPO);

    @RequestLine("POST /api/v1/internal/broker/enable")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> enableBroker(EnableBrokerPO enableBrokerPO);

    @RequestLine("POST /api/v1/internal/broker/edit")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> editBroker(EditBrokerPO editBrokerPO);

    @RequestLine("POST /api/v1/user/has_set_password_ok")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Boolean> hasSetPasswordOk(OrgIdPO orgIdPO);

    @RequestLine("POST /api/v1/user/send_set_password_email")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Boolean> sendSetPasswordEmail(SetPasswordEmailPO po);


    @RequestLine("POST /api/v1/internal/broker/add_broker_kyc_config")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> addBrokerKycConfig(AddBrokerKycConfigPO.BrokerKycConfigItem configItem);

    @RequestLine("POST /api/v1/internal/broker/broker_kyc_config_list")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<List<AddBrokerKycConfigPO.BrokerKycConfigItem>> getBrokerKycConfigs(IdPO po);

    @RequestLine("POST /api/v1/internal/broker/delete/symbol/market/account")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Boolean> deleteSymbolMarketAccount(DeleteMarketAccountPO po);

    @RequestLine("POST /api/v1/internal/broker/query/symbol/market/account/list")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<List<SymbolMarketAccountRes>> querySymbolMarketAccountList(QueryMarketAccountPO po);

    @RequestLine("POST /api/v1/internal/broker/save/symbol/market/account")
    @Headers({"Content-Type: application/json", "AdminRequest: true"})
    AdminResultRes<Object> saveSymbolMarketAccount(SymbolMarketAccountPO po);

//----平台账户操作
//    @RequestLine("POST /api/v1/platform_account/assets")
//    @Headers("Content-Type: application/json")
//    ResultModel<Map<String,Object>> getAssets(PlatformAccountAssetsPO po);
//
//    @RequestLine("POST /api/v1/platform_account/check_bind")
//    @Headers("Content-Type: application/json")
//    ResultModel checkBindAccount(PlatformAccountBindAccountPO po);
//
//    @RequestLine("POST /api/v1/platform_account/send_validate_code")
//    @Headers("Content-Type: application/json")
//    ResultModel sendBindAccountValidateCode(PlatformAccountBindAccountPO po);
//
//    @RequestLine("POST /api/v1/platform_account/bind")
//    @Headers("Content-Type: application/json")
//    ResultModel bindAccount(PlatformAccountBindAccountPO po);
    //----sms sign

//    @RequestLine("POST /api/v1/internal/sms_sign/create")
//    @Headers("Content-Type: application/json")
//    ResultModel<Boolean> createSmsSign(BorkerSmsSignCreatePO po);
}
