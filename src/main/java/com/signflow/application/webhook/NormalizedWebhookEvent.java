package com.signflow.application.webhook;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.WebhookEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Getter
public class NormalizedWebhookEvent {

    private String envelopeExternalId;

    private String signerExternalId;

    private ProviderSignature provider;

    private WebhookEventType eventType;

    private String providerEvent;

    private String providerStatus;

    private LocalDateTime occurredAt;

    private Map<String, Object> metadata;
}
