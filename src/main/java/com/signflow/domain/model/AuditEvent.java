package com.signflow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditEvent {
    private String eventType;
    private String userId;
    private String userName;
    private String ipAddress;
    private String deviceType;
    private String platform;
    private String latitude;
    private String longitude;
    private LocalDateTime occurredAt;
    private String envelopeStatus;
    private String signerStatus;
}
