package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignViewResponseDTO(
        @JsonProperty("url") String url
) {}
