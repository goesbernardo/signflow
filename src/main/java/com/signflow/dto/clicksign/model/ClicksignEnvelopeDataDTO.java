package com.signflow.dto.clicksign.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClicksignEnvelopeDataDTO {

    @NotEmpty(message = "O tipo não pode estar vazio")
    @NotNull(message = "o preenchimento do campo é obrigatório")
    private String type = "envelopes";
    
    @NotNull(message = "Os atributos do envelope são obrigatórios")
    @NotEmpty(message = "campo não pode ser enviado vazio")
    private ClicksignEnvelopeAttributesDTO attributes;
    
}
