package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignTabItemResponseDTO(
        @JsonProperty("tabId") String tabId,
        @JsonProperty("tabType") String tabType,
        @JsonProperty("documentId") String documentId,
        @JsonProperty("recipientId") String recipientId,
        @JsonProperty("tabLabel") String tabLabel,
        @JsonProperty("pageNumber") String pageNumber
) {}
