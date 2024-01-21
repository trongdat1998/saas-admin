package io.bhex.saas.admin.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformBrokerTokenDTO implements Serializable {

    private Long brokerId;
    private String brokerName;
    private Long exchangeId;
    private String tokenId;
    private String tokenName;
    private String tokenFullName;
    private String tokenIcon;



    private Integer allowDeposit;
    private Integer allowWithdraw;
    private Integer status;
    private Integer customOrder;
    private Long created;
    private Long updated;
    private Integer category; //1主类别，2创新类别, 3期权, 4期货

}
