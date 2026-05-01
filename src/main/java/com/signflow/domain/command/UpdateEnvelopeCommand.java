package com.signflow.domain.command;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEnvelopeCommand {
    @NotEmpty(message = "O nome do envelope não pode estar vazio")
    private String name;
}
