package io.bhex.saas.admin.service.impl;

import io.bhex.saas.admin.config.AwsPublicStorageConfig;
import io.bhex.saas.admin.service.ImageUtilService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.service.impl
 * @Author: ming.xu
 * @CreateDate: 26/09/2018 11:31 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Service
public class ImageUtilServiceImpl implements ImageUtilService {

    private final AwsPublicStorageConfig awsPublicStorageConfig;

    @Autowired
    public ImageUtilServiceImpl(@Qualifier("awsPublicStorageConfig") AwsPublicStorageConfig awsPublicStorageConfig) {
        this.awsPublicStorageConfig = awsPublicStorageConfig;
    }

    @Override
    public String getImageUrl(String path) {
        if (StringUtils.isNotEmpty(path)) {
            boolean b = path.startsWith(awsPublicStorageConfig.getStaticUrl());
            if (!b) {
                path = awsPublicStorageConfig.getStaticUrl() + path;
            }
        }
        return path;
    }

    @Override
    public String getImagePath(String url) {
        if (StringUtils.isNotEmpty(url)) {
            boolean b = url.startsWith(awsPublicStorageConfig.getStaticUrl());
            if (b) {
                url = url.replace(awsPublicStorageConfig.getStaticUrl(), "");
            }
        }
        return url;
    }
}
