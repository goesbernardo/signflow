package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignTabsResponseDTO(
        @JsonProperty("tabId") String tabId,
        @JsonProperty("tabType") String tabType,
        @JsonProperty("documentId") String documentId,
        @JsonProperty("recipientId") String recipientId,
        @JsonProperty("createdDateTime") String createdDateTime,
        @JsonProperty("signHereTabs") List<DocuSignTabItemResponseDTO> signHereTabs,
        @JsonProperty("initialHereTabs") List<DocuSignTabItemResponseDTO> initialHereTabs
) {}
