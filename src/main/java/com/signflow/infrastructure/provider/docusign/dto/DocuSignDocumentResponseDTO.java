package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignDocumentResponseDTO(
        @JsonProperty("documentId") String documentId,
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("order") String order,
        @JsonProperty("uri") String uri
) {}
