package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClickSignCreateDocumentAttributesDTO {

    @NotBlank(message = "O nome do arquivo é obrigatório.")
    private String filename;

    @NotBlank(message = "O conteúdo base64 é obrigatório.")
    @JsonProperty("content_base64")
    private String contentBase64;
}
