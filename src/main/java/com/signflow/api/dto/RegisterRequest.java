package com.signflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RegisterRequest(
        @NotBlank(message = "Username é obrigatório")
        @Size(min = 4, message = "Username deve ter no mínimo 4 caracteres")
        @Schema(example = "joao.silva")
        String username,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email deve ser válido")
        @Schema(example = "joao.silva@email.com")
        String email,

        @NotBlank(message = "Senha é obrigatória")
        @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
                 message = "Senha deve conter letras, números e caracteres especiais")
        @Schema(example = "Senha@123")
        String password,

        @NotBlank(message = "Nome é obrigatório")
        @Schema(example = "João da Silva")
        String name,

        @NotNull(message = "Consentimento LGPD é obrigatório")
        @Schema(description = "Data e hora do consentimento LGPD")
        LocalDateTime consentAt
) {}
