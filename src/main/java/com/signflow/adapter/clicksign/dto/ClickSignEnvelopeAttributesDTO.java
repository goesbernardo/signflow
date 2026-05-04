package com.signflow.adapter.clicksign.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignEnvelopeAttributesDTO(
    @NotNull(message = "Os atributos do envelope são obrigatórios")
    @NotEmpty(message = "campo não pode ser enviado vazio")
    String name
) {}
