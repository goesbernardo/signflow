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

        @NotBlank(message = "senha é obrigatória")
        @Size(min = 8, message = "senha deve ter no mínimo 12 caracteres")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                 message = "senha deve ter no mínimo 12 caracteres e conter letras maiúsculas, minúsculas, números e caracteres especiais")
        @Schema(example = "SenhaForte@2026")
        String password,

        @NotBlank(message = "Nome é obrigatório")
        @Schema(example = "João da Silva")
        String name,

        @NotBlank(message = "Tenant ID é obrigatório")
        @Schema(example = "CLIENT_A")
        String tenantId
) {}
