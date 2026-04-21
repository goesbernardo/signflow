package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SignatureClickSignRequirementAttributesResponseDTO {

    private String action;
    private String role;
    private String auth;
    private String name;
    private String description;

    private Boolean required;
    private String status;

    @JsonProperty("created_at")
    private String createdAt;
    @JsonProperty("updated_at")
    private String updatedAt;
}
