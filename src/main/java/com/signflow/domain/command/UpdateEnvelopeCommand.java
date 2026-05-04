package com.signflow.domain.command;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record UpdateEnvelopeCommand(
    @NotEmpty(message = "O nome do envelope não pode estar vazio")
    String name
) {}
