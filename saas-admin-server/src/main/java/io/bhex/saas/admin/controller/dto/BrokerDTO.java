package io.bhex.saas.admin.controller.dto;

import io.bhex.saas.admin.model.Broker;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

/**
 * @ProjectName: broker-admin
 * @Package: io.bhex.broker.admin.controller.dto
 * @Author: ming.xu
 * @CreateDate: 09/08/2018 11:24 AM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Data
public class BrokerDTO {

    @NotEmpty
    private String name;

    private String company;

    @NotEmpty
    @Email(regexp = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
    private String email;


    private String nationalCode;
    private String phone;

    //private String host;

    private String contact;

    private String basicInfo;

    @NotEmpty
    private String apiDomain;

//    @DecimalMin(value = "0")
//    @DecimalMax(value = "50")
//    @NotNull(message = "{javax.validation.constraints.NotEmpty.message}")
//    private BigDecimal saasFeeRate;

    private Long instanceId;

    private Long exchangeId = 0L;

    private Long dueTime = 0L; //过期时间，0-代表未设置

    public Broker toBroker() {
        return Broker.builder()
                .name(name)
                .company(company)
                .email(email)
                .phone(phone)
                //.host(host)
                .contact(contact)
                .basicInfo(basicInfo)
               // .saasFeeRate(saasFeeRate)
                .instanceId(instanceId)
                .apiDomain(apiDomain)
                .build();
    }
}
