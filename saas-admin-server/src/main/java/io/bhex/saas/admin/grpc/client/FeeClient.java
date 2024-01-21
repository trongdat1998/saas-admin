package io.bhex.saas.admin.grpc.client;

import io.bhex.base.account.GetSaasCommissionFeeRequest;
import io.bhex.base.account.GetSaasCommissionFeeResponse;
import io.bhex.base.account.UpdateSaasCommissionFeeRequest;
import io.bhex.base.account.UpdateSaasCommissionFeeResponse;

/**
 * @ProjectName: saas-admin
 * @Package: io.bhex.saas.admin.grpc.client
 * @Author: ming.xu
 * @CreateDate: 2019/7/18 6:12 PM
 * @Copyright（C）: 2019 BHEX Inc. All rights reserved.
 */
public interface FeeClient {
    
    //获取 - 交易所 Saas佣金分成比率
    GetSaasCommissionFeeResponse getSaasCommissionFee (GetSaasCommissionFeeRequest request);

    //更新 - 交易所 Saas佣金分成比率
    UpdateSaasCommissionFeeResponse updateSaasCommissionFee (UpdateSaasCommissionFeeRequest request);
}
