package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignEnvelopeResponseDTO(
        @JsonProperty("envelopeId") String envelopeId,
        @JsonProperty("status") String status,
        @JsonProperty("emailSubject") String emailSubject,
        @JsonProperty("createdDateTime") String createdDateTime,
        @JsonProperty("lastModifiedDateTime") String lastModifiedDateTime,
        @JsonProperty("completedDateTime") String completedDateTime,
        @JsonProperty("voidedDateTime") String voidedDateTime,
        @JsonProperty("declinedDateTime") String declinedDateTime
) {}
