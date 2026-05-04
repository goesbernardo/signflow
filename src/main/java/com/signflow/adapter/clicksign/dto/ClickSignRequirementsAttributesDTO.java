package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignRequirementsAttributesDTO(String action) {}
