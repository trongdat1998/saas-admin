package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Description:
 * @Date: 2020/9/21 上午11:05
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@Table(name = "tb_symbol_match_transfer")
public class SymbolMatchTransfer {

    @Id
    private Long id;
    private Long sourceExchangeId;
    private Long sourceBrokerId;
    private String sourceBrokerName;
    private Long matchExchangeId;
    private Long matchBrokerId;
    private String matchExchangeName;
    private String matchBrokerName;
    private String symbolId;
    private Integer category;
    private String remark;
    private Integer enable;
    private Long createdAt;
    private Long updatedAt;
}
