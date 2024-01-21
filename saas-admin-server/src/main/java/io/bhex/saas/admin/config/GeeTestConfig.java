package io.bhex.saas.admin.config;

import io.bhex.broker.common.api.client.geetest.GeeTestProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.config
 * @Author: ming.xu
 * @CreateDate: 23/11/2018 3:55 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
@Component
@ConfigurationProperties(prefix = "gee-test-config")
public class GeeTestConfig {

    private GeeTestProperties geeTest = new GeeTestProperties();
}
