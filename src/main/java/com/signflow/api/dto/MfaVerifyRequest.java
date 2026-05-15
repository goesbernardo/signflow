package com.signflow.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(
    @NotBlank String mfaToken,
    @NotBlank String code
) {}
