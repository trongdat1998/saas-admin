package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author JinYuYuan
 * @description
 * @date 2021-01-15 13:58
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tb_deposit_receipt_apply_record")
public class DepositReceiptApplyRecord {
    @Id
    @GeneratedValue(generator="JDBC")
    public Long id;

    public Long orgId;

    public String tokenId;

    public Long orderId;

    public Long created;

    public Long updated;

}
