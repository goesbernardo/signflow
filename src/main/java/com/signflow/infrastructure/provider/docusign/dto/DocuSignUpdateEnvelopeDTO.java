package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignUpdateEnvelopeDTO(
        @JsonProperty("emailSubject") String emailSubject,
        @JsonProperty("status") String status,
        @JsonProperty("voidedReason") String voidedReason
) {}
