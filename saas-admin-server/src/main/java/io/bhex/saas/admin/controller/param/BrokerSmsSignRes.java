package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @Description: 短信签名
 * @Date: 2018/10/10 上午10:06
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Data
public class BrokerSmsSignRes {

    private Long id;

    private Long brokerId;

    private String sign;

    private String brokerName;

    @JsonIgnore
    private String language;




}
