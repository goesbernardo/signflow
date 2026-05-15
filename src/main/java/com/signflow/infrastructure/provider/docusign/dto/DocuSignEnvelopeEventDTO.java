package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record DocuSignEnvelopeEventDTO(
        @JsonProperty("envelopeEventStatusCode") String envelopeEventStatusCode,
        @JsonProperty("includeDocuments") Boolean includeDocuments
) {}
