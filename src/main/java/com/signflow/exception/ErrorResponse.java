package com.signflow.exception.domain;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Jacksonized
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String error,
    String message,
    String path,
    List<ErroDetail> details
) {}
