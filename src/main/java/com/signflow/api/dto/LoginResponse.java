package com.signflow.api.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LoginResponse(
    String accessToken, 
    String refreshToken, 
    String mfaToken, 
    boolean mfaRequired
) {}
