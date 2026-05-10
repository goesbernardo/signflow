package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.br.CPF;

@Builder
@Jacksonized
public record ClickSignCreateSignAttributesDTO(
    String name,
    @Email
    String email,
    @CPF
    String documentation,
    @JsonProperty("has_documentation")
    Boolean hasDocumentation,
    Boolean refusable,
    String group,
    @JsonProperty("location_required_enabled")
    Boolean locationRequiredEnabled,
    @JsonProperty("phone_number")
    String phoneNumber,
    @JsonProperty("communicate_events")
    ClickSignCreateSignEventsDTO communicateEvents
) {}
