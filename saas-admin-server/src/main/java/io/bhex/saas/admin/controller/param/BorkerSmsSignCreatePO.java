package io.bhex.saas.admin.controller.param;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @Description:创建短信签名
 * @Date: 2018/10/10 上午11:17
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
public class BorkerSmsSignCreatePO {

    private String brokerName;

    private Long orgId;

    @NotEmpty
    @Length(max = 20)
    private String sign;


    private String language;
}
