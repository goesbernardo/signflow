package com.signflow.application.webhook;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.enums.WebhookEventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OutboundWebhookPayload {
    private String envelopeId;
    private ProviderSignature provider;
    private WebhookEventType eventType;
    private Status status;
    private LocalDateTime occurredAt;
}
