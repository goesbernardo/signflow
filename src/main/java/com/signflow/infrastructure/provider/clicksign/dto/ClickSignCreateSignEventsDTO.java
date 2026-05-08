package com.signflow.infrastructure.provider.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignCreateSignEventsDTO(
    @JsonProperty("signature_request")
    String signatureRequest,
    @JsonProperty("signature_reminder")
    String signatureReminder,
    @JsonProperty("document_signed")
    String documentSigned
) {}
