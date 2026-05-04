package com.signflow.exception.domain;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ErroDetail(
    String field,
    String message,
    String code
) {}
