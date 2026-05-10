package com.signflow.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OutboundWebhookDeliveryResponse {
    private Long id;
    private String url;
    private String payload;
    private String status;
    private int attempts;
    private Integer httpStatusCode;
    private LocalDateTime lastAttemptAt;
    private LocalDateTime createdAt;
}
