package com.signflow.dto.clicksign;

import com.signflow.dto.clicksign.model.ClicksignEnvelopeDataDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClickSignCreateEnvelopeRequestDTO {

    @NotNull(message = "O campo data não pode ser nulo")
    @Valid
    private ClicksignEnvelopeDataDTO data;

}
