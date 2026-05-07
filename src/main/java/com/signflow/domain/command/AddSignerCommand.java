package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AddSignerCommand(
    String name,
    @Email
    String email,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "000.000.000-00")
    String documentation,
    Boolean hasDocumentation,
    @JsonProperty("phone_number")
    @Pattern(regexp = "\\d{10,11}", message = "O número de telefone deve possuir 10 ou 11 números")
    String phoneNumber,
    String delivery,
    String requestSignature
) {}
