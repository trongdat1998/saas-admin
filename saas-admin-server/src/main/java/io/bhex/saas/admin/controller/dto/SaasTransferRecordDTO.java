package io.bhex.saas.admin.controller.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.DecimalOutputSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SaasTransferRecordDTO extends VerifyFlowRecordDTO {


    private boolean tmplModel;

    //是否允许多券商间转账
    private boolean moreBrokers = false;

    private String tokenId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long transferInAccount; //转入账户

    private List<TransferOutAccount> transferOutAccounts; //转出账户

    @Data
    public static class TransferOutAccount {

        private Long orgId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long accountId;

        @JsonSerialize(using = DecimalOutputSerialize.class)
        private BigDecimal amount;

        private String tokenId;

        private Integer status;

    }

}
