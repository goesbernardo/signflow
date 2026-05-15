package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignWebhookPayloadDTO(
        @JsonProperty("event") String event,
        @JsonProperty("apiVersion") String apiVersion,
        @JsonProperty("uri") String uri,
        @JsonProperty("retryCount") String retryCount,
        @JsonProperty("configurationId") String configurationId,
        @JsonProperty("generatedDateTime") String generatedDateTime,
        @JsonProperty("data") DocuSignWebhookDataDTO data
) {}
