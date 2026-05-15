package com.signflow.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "Senha atual é obrigatória")
    @Schema(example = "SenhaAntiga@123")
    String currentPassword,

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 12, message = "A nova senha deve ter no mínimo 12 caracteres")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
             message = "Senha deve ter no mínimo 12 caracteres e conter letras maiúsculas, minúsculas, números e caracteres especiais")
    @Schema(example = "NovaSenhaForte@2026")
    String newPassword
) {}
