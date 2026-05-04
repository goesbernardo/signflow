package com.signflow.api.dto;

import com.signflow.enums.Status;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record EnvelopeTimelineResponse(
    Status previousStatus,
    Status newStatus,
    String source,
    LocalDateTime occurredAt
) {}
