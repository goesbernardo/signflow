package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookAttributesDTO {

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private String status;          // "running" | "completed" | "canceled" | "draft"

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;
}

