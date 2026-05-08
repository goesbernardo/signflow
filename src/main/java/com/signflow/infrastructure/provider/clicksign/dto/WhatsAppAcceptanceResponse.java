package com.signflow.infrastructure.provider.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;

@Builder
@Jacksonized
public record WhatsAppAcceptanceResponse(
        String externalId,
        String title,
        String signerPhone,
        String status,
        String statusDescription,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    /**
     * Descrição amigável para cada status da ClickSign.
     */
    public static String describeStatus(String status) {
        return switch (status != null ? status : "") {
            case "enqueued" -> "Aceite criado e aguardando envio via WhatsApp.";
            case "sent" -> "Aceite enviado via WhatsApp ao destinatário.";
            case "completed" -> "Aceite confirmado pelo destinatário.";
            case "refused" -> "Aceite recusado pelo destinatário.";
            case "expired" -> "Aceite expirado sem resposta.";
            case "error" -> "Erro no envio via WhatsApp.";
            case "canceled" -> "Aceite cancelado.";
            default -> "Status desconhecido.";
        };
    }
}
