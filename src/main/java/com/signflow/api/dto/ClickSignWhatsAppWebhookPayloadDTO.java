package com.signflow.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClickSignWhatsAppWebhookPayloadDTO(
        WhatsAppWebhookData data
) {
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsAppWebhookData(
            String id,
            String type,
            WhatsAppWebhookAttributes attributes
    ) {}

    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsAppWebhookAttributes(
            String status,
            String title,
            @JsonProperty("signer_name")
            String signerName,
            @JsonProperty("signer_phone")
            String signerPhone,
            @JsonProperty("sent_at")
            String sentAt,
            String created,
            String modified
    ) {}
}
