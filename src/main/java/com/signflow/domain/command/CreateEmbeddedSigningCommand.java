package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record CreateEmbeddedSigningCommand(
        @NotBlank(message = "returnUrl é obrigatório")
        @JsonProperty("return_url")
        String returnUrl,

        @NotBlank(message = "Email do signatário é obrigatório")
        @Email(message = "Email inválido")
        @JsonProperty("recipient_email")
        String recipientEmail,

        @NotBlank(message = "Nome do signatário é obrigatório")
        @JsonProperty("recipient_name")
        String recipientName,

        @NotBlank(message = "clientUserId é obrigatório — deve ser o ID interno do signatário")
        @JsonProperty("client_user_id")
        String clientUserId,

        @JsonProperty("recipient_id")
        String recipientId,

        @JsonProperty("ping_url")
        String pingUrl
) {}
