package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignSignHereTabDTO(
        @JsonProperty("documentId") String documentId,
        @JsonProperty("pageNumber") String pageNumber,
        @JsonProperty("tabLabel") String tabLabel,
        @JsonProperty("xPosition") String xPosition,
        @JsonProperty("yPosition") String yPosition
) {}
