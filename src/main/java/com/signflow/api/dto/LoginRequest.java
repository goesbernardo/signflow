package com.signflow.api.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record LoginRequest(String username, String password) {}
