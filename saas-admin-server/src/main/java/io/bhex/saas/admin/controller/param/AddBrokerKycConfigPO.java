package io.bhex.saas.admin.controller.param;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/30 下午5:34
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class AddBrokerKycConfigPO implements Serializable {

    private List<BrokerKycConfigItem> configs;

    private Long brokerId;

    @Data
    public static class BrokerKycConfigItem  implements Serializable {
        private Long brokerId;
        private Long countryId;
        private Integer kycLevel;
        private String webankAppId;
        private String webankAppSecret;
        private String webankAndroidLicense;
        private String webankIosLicense;
        private String appName;
        private String companyName;
    }

}
