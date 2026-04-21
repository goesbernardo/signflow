package com.signflow.dto.clicksign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClickSignCreateDocumentDataDTO extends BaseDataDTO {

    private String type = "documents";
    @NotNull(message = "Os atributos do documento são obrigatórios.")
    @Valid
    private ClickSignCreateDocumentAttributesDTO attributes;
}
