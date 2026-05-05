package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record CreateWhatsAppAcceptanceCommand(
        @NotBlank(message = "Título é obrigatório")
        @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
        String title,

        @NotBlank(message = "Opção de nome do remetente é obrigatória")
        @JsonProperty("sender_name_option")
        String senderNameOption,       // "user_name" | "account_name" | "user_and_account_name"

        @JsonProperty("sender_phone")
        String senderPhone,

        @NotBlank(message = "Mensagem é obrigatória")
        @Size(max = 1500, message = "Mensagem deve ter no máximo 1500 caracteres")
        String message,

        @NotBlank(message = "Telefone do signatário é obrigatório")
        @JsonProperty("signer_phone")
        @Pattern(regexp = "^55\\d{10,11}$", message = "Telefone deve estar no formato 55 + DDD + número (ex: 5521988888888)")
        String signerPhone,

        @NotBlank(message = "Nome do signatário é obrigatório")
        @Size(max = 200, message = "Nome do signatário deve ter no máximo 200 caracteres")
        @JsonProperty("signer_name")
        String signerName
) { }
