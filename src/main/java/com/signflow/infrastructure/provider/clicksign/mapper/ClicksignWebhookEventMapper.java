package com.signflow.infrastructure.provider.clicksign.mapper;

import com.signflow.application.webhook.NormalizedWebhookEvent;
import com.signflow.enums.ClickSignWebhookEvent;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.WebhookEventType;
import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class ClicksignWebhookEventMapper {

    public NormalizedWebhookEvent toNormalizedEvent(ClickSignWebhookRootPayloadDTO rootPayload) {
        if (rootPayload.data() != null) {
            return mapJsonApiFormat(rootPayload);
        } else if (rootPayload.event() != null) {
            return mapLegacyFormat(rootPayload);
        }
        throw new IllegalArgumentException("Payload do webhook da ClickSign inválido ou não reconhecido");
    }

    private NormalizedWebhookEvent mapJsonApiFormat(ClickSignWebhookRootPayloadDTO rootPayload) {
        var data = rootPayload.data();
        var attributes = data.attributes();
        ClickSignWebhookEvent clickSignEvent = ClickSignWebhookEvent.fromValue(attributes.name());

        return NormalizedWebhookEvent.builder()
                .envelopeExternalId(data.id())
                .provider(ProviderSignature.CLICKSIGN)
                .eventType(map(clickSignEvent))
                .providerEvent(attributes.name())
                .providerStatus(attributes.status())
                .occurredAt(parseDateTime(attributes.updatedAt() != null ? attributes.updatedAt() : attributes.createdAt()))
                .build();
    }

    private NormalizedWebhookEvent mapLegacyFormat(ClickSignWebhookRootPayloadDTO rootPayload) {
        var event = rootPayload.event();
        var acceptance = rootPayload.acceptance();
        ClickSignWebhookEvent clickSignEvent = ClickSignWebhookEvent.fromValue(event.name());

        Map<String, Object> metadata = new HashMap<>();
        if (acceptance != null) {
            metadata.put("signerName", acceptance.signerName());
            metadata.put("signerPhone", acceptance.signerPhone());
        }

        return NormalizedWebhookEvent.builder()
                .envelopeExternalId(acceptance != null ? acceptance.key() : null)
                .provider(ProviderSignature.CLICKSIGN)
                .eventType(map(clickSignEvent))
                .providerEvent(event.name())
                .providerStatus(acceptance != null ? acceptance.status() : null)
                .occurredAt(acceptance != null ? parseDateTime(acceptance.createdAt()) : LocalDateTime.now())
                .metadata(metadata)
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public WebhookEventType map(ClickSignWebhookEvent event) {

        return switch (event) {

            case SIGN ->
                    WebhookEventType.DOCUMENT_SIGNED;

            case AUTO_CLOSE,
                 DOCUMENT_CLOSED ->
                    WebhookEventType.DOCUMENT_COMPLETED;

            case REFUSAL ->
                    WebhookEventType.DOCUMENT_REJECTED;

            case CANCEL ->
                    WebhookEventType.DOCUMENT_CANCELED;

            case DEADLINE ->
                    WebhookEventType.DOCUMENT_EXPIRED;

            case ADD_SIGNER ->
                    WebhookEventType.SIGNER_ADDED;

            case REMOVE_SIGNER ->
                    WebhookEventType.SIGNER_REMOVED;

            case BIOMETRIC_REFUSED,
                 FACEMATCH_REFUSED,
                 LIVENESS_REFUSED ->
                    WebhookEventType.BIOMETRIC_REFUSED;

            default ->
                    WebhookEventType.UNKNOWN;
        };
    }
}
