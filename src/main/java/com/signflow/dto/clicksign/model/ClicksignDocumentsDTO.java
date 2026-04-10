package com.signflow.dto.clicksign.model;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClicksignDocumentsDTO {

    @NotNull(message = "Os atributos do envelope são obrigatórios")
    @NotEmpty(message = "campo não pode ser enviado vazio")
    private List<ClicksignDocumentDataDTO> data;


}
