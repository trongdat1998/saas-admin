package io.bhex.saas.admin.controller.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.bhex.bhop.common.util.DecimalOutputSerialize;
import io.bhex.bhop.common.util.validation.CommonInputValid;
import io.bhex.bhop.common.util.validation.TokenValid;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AddSaasTransferPO {

    private Long reqId;

    @NotEmpty
    @CommonInputValid
    private String title;

    @CommonInputValid(maxLength = 512)
    private String description;

    //是否允许多券商间转账
    private boolean moreBrokers = false;

    @TokenValid(allowEmpty = true)
    private String tokenId = "";

    @NotNull
    private Long transferInAccount; //转入账户

    private List<@Valid TransferOutAccount> transferOutAccounts; //转出账户

    @Data
    public static class TransferOutAccount {

        @JsonIgnore
        private Long orgId;

        @JsonSerialize(using = ToStringSerializer.class)
        private Long accountId;

        @Min(0)
        @JsonSerialize(using = DecimalOutputSerialize.class)
        private BigDecimal amount;

        @TokenValid(allowEmpty = true)
        private String tokenId;

    }

    private String sequenceId;

    private Integer authType;

    private String verifyCode;
}
