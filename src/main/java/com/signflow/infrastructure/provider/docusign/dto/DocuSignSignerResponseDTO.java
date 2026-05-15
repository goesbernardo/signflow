package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignSignerResponseDTO(
        @JsonProperty("recipientId") String recipientId,
        @JsonProperty("name") String name,
        @JsonProperty("email") String email,
        @JsonProperty("status") String status,
        @JsonProperty("signedDateTime") String signedDateTime,
        @JsonProperty("createdDateTime") String createdDateTime,
        @JsonProperty("deliveredDateTime") String deliveredDateTime,
        @JsonProperty("routingOrder") String routingOrder
) {}
