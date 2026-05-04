package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AddSignerCommand(
    String name,
    String email,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "000.000.000-00")
    String documentation,
    Boolean hasDocumentation,
    String phone,
    String delivery,
    String requestSignature
) {}
