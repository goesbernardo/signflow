package com.signflow.dto.clicksign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
public class ClickSignCreateDocumentDataDTO  {

    private String type = "documents";
    @NotNull(message = "Os atributos do documento são obrigatórios.")
    @Valid
    private ClickSignCreateDocumentAttributesDTO attributes;
}
