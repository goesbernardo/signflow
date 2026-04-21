package com.signflow.dto.clicksign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClickSignCreateDocumentDTO {

    @NotNull(message = "O campo data é obrigatório.")
    @Valid
    private ClickSignCreateDocumentDataDTO data;
}
