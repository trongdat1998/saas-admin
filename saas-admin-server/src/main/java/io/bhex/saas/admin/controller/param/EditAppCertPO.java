package io.bhex.saas.admin.controller.param;



import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Description:
 * @Date: 2019/6/13 下午4:29
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class EditAppCertPO {
    @NotNull
    private Long orgId;

    @Valid
    private List<AppCertPO> items;

    @Valid
    @Data
    public static class AppCertPO {
        @NotEmpty
        private String bundleId;

        private String developerAppId;

        @NotEmpty
        private String developerSecretKey;

        private String appChannel;

        @NotEmpty
        private String pushChannel;

        private String password;

        private String fcmDatabaseUrl;
    }


}
