package com.signflow.api.dto;

import lombok.Builder;

@Builder
public record AdminRegisterResponse(
    String name,
    String role,
    String message
) {}
