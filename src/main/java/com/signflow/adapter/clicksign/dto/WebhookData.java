package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookData(
    @JsonProperty("id")
    String id,
    @JsonProperty("type")
    String type,
    @JsonProperty("attributes")
    WebhookAttributesDTO attributes
) {}
