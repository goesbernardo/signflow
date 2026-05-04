package com.signflow.domain.command;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AddRequirementCommand(
    String signerId,
    String documentId,
    String action
) {}
