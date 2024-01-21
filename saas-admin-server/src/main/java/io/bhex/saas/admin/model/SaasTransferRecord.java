package io.bhex.saas.admin.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Table(name = "tb_saas_transfer_record")
public class SaasTransferRecord {

    public static final Integer STATUS_INIT = 0;
    public static final int STATUS_LOCK_BALANCE = 1;
    public static final Integer STATUS_TRANSFER_OUT_SUCCESS = 2; //结束

    public static final int STATUS_REJECTED_UNLOCK_BALANCE = 3;

    public static final int STATUS_FAILED_UNLOCK_BALANCE = 4;

    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;

    private Long bizRecordId;

    private Long orgId;

    private Long accountId;

    private Long targetOrgId;

    private Long targetAccountId;

    private String tokenId;

    private BigDecimal amount;

    private Integer status; //lock suc

    private Long lockId;

    private Long createdAt;

    private Long updatedAt;


}
