package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignDocumentDTO(
        @JsonProperty("documentBase64") String documentBase64,
        @JsonProperty("documentId") String documentId,
        @JsonProperty("fileExtension") String fileExtension,
        @JsonProperty("name") String name
) {}
