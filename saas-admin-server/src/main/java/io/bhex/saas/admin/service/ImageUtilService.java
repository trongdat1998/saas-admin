package io.bhex.saas.admin.service;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.service
 * @Author: ming.xu
 * @CreateDate: 26/09/2018 11:29 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface ImageUtilService {

    /**
     * 获得图片的外网访问路径地址
     * @param path
     * @return
     */
    String getImageUrl(String path);

    /**
     * 从外网地址中获取图片的存储路径，用于存储
     * @param url
     * @return
     */
    String getImagePath(String url);
}
