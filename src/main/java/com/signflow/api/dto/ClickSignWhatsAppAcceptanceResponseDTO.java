package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public record ClickSignWhatsAppAcceptanceResponseDTO( WhatsAppAcceptanceData data
) {
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsAppAcceptanceData(
            String id,
            String type,
            WhatsAppAcceptanceAttributes attributes
    ) {}

    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WhatsAppAcceptanceAttributes(
            String title,
            @JsonProperty("sender_name") String senderName,
            @JsonProperty("sender_phone") String senderPhone,
            @JsonProperty("sender_name_option") String senderNameOption,
            String message,
            @JsonProperty("signer_phone") String signerPhone,
            @JsonProperty("signer_name") String signerName,
            String status,
            @JsonProperty("status_flow") String statusFlow,
            @JsonProperty("sent_at") String sentAt,
            String created,
            String modified) {}
}
