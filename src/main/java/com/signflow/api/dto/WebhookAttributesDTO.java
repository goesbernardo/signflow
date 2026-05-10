package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookAttributesDTO(
    @JsonProperty("name")
    String name,
    @JsonProperty("status")
    String status,
    @JsonProperty("created_at")
    String createdAt,
    @JsonProperty("updated_at")
    String updatedAt
) {}

