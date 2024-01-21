package io.bhex.saas.admin.config;

import io.bhex.broker.common.objectstorage.AwsObjectStorageProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "awsstorage.public")
@Component
public class AwsPublicStorageConfig {

    private String prefix;

    private String staticUrl;

    private AwsObjectStorageProperties aws = new AwsObjectStorageProperties();

    private String accessOsFileKey = "";


}
