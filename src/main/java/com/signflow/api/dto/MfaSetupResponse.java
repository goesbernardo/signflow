package com.signflow.api.dto;

import lombok.Builder;

@Builder
public record MfaSetupResponse(
    String secret,
    String qrCodeUri
) {}
