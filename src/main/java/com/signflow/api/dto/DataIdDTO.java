package com.signflow.adapter.clicksign.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record DataIdDTO(
    String type,
    String id
) {}
