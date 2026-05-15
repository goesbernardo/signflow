package com.signflow.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record AdminRegisterRequest(

    @NotBlank(message = "Username é obrigatório")
    @Size(min = 4, message = "Username deve ter no mínimo 4 caracteres")
    String username,

    @NotBlank(message = "Nome é obrigatório")
    String name,

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    String email,

    @NotBlank(message = "Senha é obrigatória")
    String password,

    @NotBlank(message = "TenantId é obrigatório")
    String tenantId,

    @NotBlank(message = "Role é obrigatória")
    @Pattern(
        regexp = "^(VIEWER|OPERATOR|WEBHOOK)$",
        message = "Role inválida. Valores aceitos: VIEWER, OPERATOR, WEBHOOK"
    )
    String role

) {}
