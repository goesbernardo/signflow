package com.signflow.api.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record SecurityErrorResponse(
    int status,
    String error,
    String message,
    String path
) {}
