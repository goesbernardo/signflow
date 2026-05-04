package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignCreateDocumentAttributesDTO(
    @NotBlank(message = "O nome do arquivo é obrigatório.")
    String filename,
    @NotBlank(message = "O conteúdo base64 é obrigatório.")
    @JsonProperty("content_base64")
    String contentBase64
) {}
