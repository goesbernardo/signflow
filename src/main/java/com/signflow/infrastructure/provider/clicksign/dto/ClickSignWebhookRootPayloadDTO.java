package com.signflow.infrastructure.provider.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.signflow.infrastructure.provider.clicksign.dto.WebhookAttributesDTO;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Payload REAL recebido nos callbacks de webhook da ClickSign.
 *
 * A ClickSign envia dois formatos diferentes:
 *
 * 1. Envelopes (formato JSON:API):
 * {
 *   "data": { "id": "uuid", "type": "envelopes", "attributes": { "status": "completed" } }
 * }
 *
 * 2. Aceite via WhatsApp (formato legado):
 * {
 *   "event": { "name": "acceptance_term_enqueued", "data": { ... } },
 *   "acceptance": { "key": "uuid", "status": "enqueued", "signer_name": "...", ... }
 * }
 */
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignWebhookRootPayloadDTO(

        // Formato JSON:API (envelopes e novos webhooks)
        WebhookDataWrapper data,

        // Formato legado (aceite via WhatsApp)
        EventWrapper event,
        AcceptanceData acceptance

) {
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record WebhookDataWrapper(
            String id,
            String type,
            WebhookAttributesDTO attributes
    ) {}

    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record EventWrapper(
            String name
    ) {}

    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AcceptanceData(
            String key,
            String name,
            String status,
            @JsonProperty("signer_name")
            String signerName,
            @JsonProperty("signer_phone")
            String signerPhone,
            @JsonProperty("sender_name")
            String senderName,
            @JsonProperty("sender_phone")
            String senderPhone,
    //        String content,
            @JsonProperty("sent_at")
            String sentAt,
            @JsonProperty("created_at")
            String createdAt
    ) {}
}
