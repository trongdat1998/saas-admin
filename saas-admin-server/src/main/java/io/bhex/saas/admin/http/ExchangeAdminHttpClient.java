//package io.bhex.saas.admin.http;
//
//import feign.Headers;
//import feign.RequestLine;
//import io.bhex.bhop.common.dto.param.ChangeAdminUserPO;
//import io.bhex.bhop.common.dto.param.CreateAdminUserPO;
//import io.bhex.bhop.common.dto.param.OrgIdPO;
//import io.bhex.bhop.common.dto.param.SetPasswordEmailPO;
//import io.bhex.saas.admin.http.response.AdminResultRes;
//
//public interface ExchangeAdminHttpClient {
//
//    @RequestLine("POST /api/v1/user/create_user")
//    @Headers({"Content-Type: application/json", "AdminRequest: true"})
//    AdminResultRes<Object> createUser(CreateAdminUserPO createAdminUserPO);
//
//    @RequestLine("POST /api/v1/user/change_admin_user")
//    @Headers({"Content-Type: application/json", "AdminRequest: true"})
//    AdminResultRes<Object> changeAdminUser(ChangeAdminUserPO changeAdminUserPO);
//
//    @RequestLine("POST /api/v1/user/has_set_password_ok")
//    @Headers({"Content-Type: application/json", "AdminRequest: true"})
//    AdminResultRes<Boolean> hasSetPasswordOk(OrgIdPO orgIdPO);
//
//    @RequestLine("POST /api/v1/user/send_set_password_email")
//    @Headers({"Content-Type: application/json", "AdminRequest: true"})
//    AdminResultRes<Boolean> sendSetPasswordEmail(SetPasswordEmailPO po);
//}
