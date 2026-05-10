package com.signflow.adapter.clicksign.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignResponseDataDTO(
    String id,
    String type,
    ClickSignResponseAttributesDTO attributes
) {}
