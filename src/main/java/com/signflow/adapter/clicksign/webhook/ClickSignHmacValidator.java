package com.signflow.adapter.clicksign.webhook;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Slf4j
@Component
public class ClickSignHmacValidator {

    private static final String ALGORITHM = "HmacSHA256";

    @Value("${signflow.providers.clicksign.webhook-secret}")
    private String webhookSecret;

    /**
     * Verifica se o HMAC recebido no header corresponde ao payload.
     *
     * @param receivedHmac valor do header {@code X-Clicksign-Hmac-Sha256}
     * @param rawPayload   corpo bruto da requisição (antes de desserializar)
     * @return true se a assinatura for válida
     */
    public boolean isValid(String receivedHmac, String rawPayload) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            log.warn("CLICKSIGN_WEBHOOK_SECRET não configurado — validação HMAC desabilitada. " +
                    "Configure a variável de ambiente antes de ir para produção.");
            return true; // permissivo apenas sem secret configurado
        }

        if (receivedHmac == null || receivedHmac.isBlank()) {
            log.warn("Webhook recebido sem header X-Clicksign-Hmac-Sha256.");
            return false;
        }

        try {
            String computed = compute(rawPayload);
            boolean valid = constantTimeEquals(receivedHmac.toLowerCase(), computed);

            if (!valid) {
                log.warn("Assinatura HMAC inválida. Recebido: {}, Esperado: {}", receivedHmac, computed);
            }

            return valid;

        } catch (Exception e) {
            log.error("Erro ao validar HMAC do webhook ClickSign", e);
            return false;
        }
    }

    private String compute(String payload)
            throws NoSuchAlgorithmException, InvalidKeyException {

        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(
                webhookSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(keySpec);
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);   // Java 17+ — hex lowercase sem separador
    }

    /**
     * Comparação em tempo constante para evitar timing attacks.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
