package io.bhex.saas.admin.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_exchange_op_record")
@AllArgsConstructor
@NoArgsConstructor
public class ExchangeOpRecord {

    @Id
    private Long id;

    private Long exchangeId;

    private Long saasExchangeId;

    private int opType;

    private String reqContent;

    private String resContent;

    private Long createdAt;

    private Long opSaasAdminId;

    private String remark;

}
