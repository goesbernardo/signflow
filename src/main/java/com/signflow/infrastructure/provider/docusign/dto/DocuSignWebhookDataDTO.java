package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignWebhookDataDTO(
        @JsonProperty("accountId") String accountId,
        @JsonProperty("envelopeId") String envelopeId,
        @JsonProperty("userId") String userId,
        @JsonProperty("envelopeSummary") DocuSignEnvelopeResponseDTO envelopeSummary
) {}
