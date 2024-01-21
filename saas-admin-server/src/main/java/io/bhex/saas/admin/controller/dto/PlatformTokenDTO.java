/*************************************
 * @项目名称: saas-admin-parent
 * @文件名称: PlatformTokenDTO
 * @Date 2019/12/05
 * @Author fred.wang@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 ***************************************/
package io.bhex.saas.admin.controller.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created on 2019/12/5
 *
 * @author wangxuefei
 */
@Data
public class PlatformTokenDTO implements Serializable {

    private String tokenId;
    private Boolean allowDeposit;
    private Boolean allowWithdraw;
    private Integer tokenType;
    private Boolean addressNeedTag;
}
