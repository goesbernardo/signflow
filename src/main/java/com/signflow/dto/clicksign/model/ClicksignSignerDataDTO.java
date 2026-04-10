package com.signflow.dto.clicksign.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClicksignSignerDataDTO {

    @NotNull(message = "Os atributos do envelope são obrigatórios")
    @NotEmpty(message = "campo não pode ser enviado vazio")
    private String type = "signers";

    @NotNull(message = "Os atributos do envelope são obrigatórios")
    @NotEmpty(message = "campo não pode ser enviado vazio")
    private ClicksignSignerAttributesDTO attributes;
}
