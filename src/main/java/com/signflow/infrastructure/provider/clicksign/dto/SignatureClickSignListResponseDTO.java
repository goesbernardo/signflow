package com.signflow.infrastructure.provider.clicksign.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public record SignatureClickSignListResponseDTO(
    List<ClickSignResponseDataDTO> data
) {}
