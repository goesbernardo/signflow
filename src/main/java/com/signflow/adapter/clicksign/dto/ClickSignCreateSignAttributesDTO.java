package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignCreateSignAttributesDTO(
    String name,
    String email,
    String documentation,
    @JsonProperty("has_documentation")
    Boolean hasDocumentation,
    Boolean refusable,
    String group,
    @JsonProperty("location_required_enabled")
    Boolean locationRequiredEnabled,
    @JsonProperty("communicate_events")
    ClickSignCreateSignEventsDTO communicateEvents
) {}
