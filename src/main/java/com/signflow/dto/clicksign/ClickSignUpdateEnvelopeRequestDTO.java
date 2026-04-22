package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClickSignUpdateEnvelopeRequestDTO {

    @NotNull(message = "O campo data não pode ser null")
    @Valid
    private ClickSignEnvelopeDataDTO data;

    @JsonIgnore
    private UUID id;

}
