package io.bhex.saas.admin.controller.dto;


import lombok.Data;

@Data
public class DeliveryRecordDTO {
    private Long orgId;
    private String channel;

    private String receiver;
    private String messageId;
    private String deliveryStatus;

    private String content;
    private Long created;
    private Long deliveriedAt;
    private String description;
    private String bizType;
}
