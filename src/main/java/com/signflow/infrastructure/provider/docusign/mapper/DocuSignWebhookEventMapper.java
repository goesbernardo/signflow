package com.signflow.infrastructure.provider.docusign.mapper;

import com.signflow.application.webhook.NormalizedWebhookEvent;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.WebhookEventType;
import com.signflow.infrastructure.provider.docusign.dto.DocuSignWebhookPayloadDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocuSignWebhookEventMapper {

    private final DocuSignMapper mapper;

    public NormalizedWebhookEvent toNormalizedEvent(DocuSignWebhookPayloadDTO payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload do webhook DocuSign não pode ser nulo");
        }

        String envelopeId = null;
        String providerStatus = null;
        LocalDateTime occurredAt = LocalDateTime.now();
        Map<String, Object> metadata = new HashMap<>();

        if (payload.data() != null) {
            envelopeId = payload.data().envelopeId();

            if (payload.data().envelopeSummary() != null) {
                providerStatus = payload.data().envelopeSummary().status();
            }

            metadata.put("accountId", payload.data().accountId() != null ? payload.data().accountId() : "unknown");
        }

        if (payload.generatedDateTime() != null) {
            occurredAt = mapper.parseLocalDateTime(payload.generatedDateTime());
        }

        return NormalizedWebhookEvent.builder()
                .envelopeExternalId(envelopeId)
                .provider(ProviderSignature.DOCUSIGN)
                .eventType(map(payload.event()))
                .providerEvent(payload.event())
                .providerStatus(providerStatus)
                .occurredAt(occurredAt)
                .metadata(metadata)
                .build();
    }

    public WebhookEventType map(String event) {
        if (event == null) return WebhookEventType.UNKNOWN;

        return switch (event.toLowerCase()) {
            case "envelope-sent"       -> WebhookEventType.DOCUMENT_SENT;
            case "envelope-delivered"  -> WebhookEventType.DOCUMENT_VIEWED;
            case "envelope-signed"     -> WebhookEventType.DOCUMENT_SIGNED;
            case "envelope-completed"  -> WebhookEventType.DOCUMENT_COMPLETED;
            case "envelope-declined",
                 "recipient-declined"  -> WebhookEventType.DOCUMENT_REJECTED;
            case "envelope-voided"     -> WebhookEventType.DOCUMENT_CANCELED;
            case "recipient-sent"      -> WebhookEventType.DOCUMENT_SENT;
            case "recipient-completed" -> WebhookEventType.DOCUMENT_SIGNED;
            default                    -> WebhookEventType.UNKNOWN;
        };
    }
}
