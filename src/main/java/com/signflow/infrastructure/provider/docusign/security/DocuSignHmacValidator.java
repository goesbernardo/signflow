package com.signflow.infrastructure.provider.docusign.security;

import com.signflow.config.DocuSignProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Valida a assinatura HMAC-SHA256 dos webhooks DocuSign Connect.
 *
 * DocuSign envia a assinatura no header X-DocuSign-Signature-1 como Base64(HMAC-SHA256(secret, payload)).
 * O secret é configurado no painel DocuSign Connect → HMAC Secret.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "signflow.providers.docusign", name = "enabled", havingValue = "true")
public class DocuSignHmacValidator {

    private static final String ALGORITHM = "HmacSHA256";

    private final DocuSignProperties props;

    /**
     * @param receivedSignature valor do header X-DocuSign-Signature-1
     * @param rawPayload        corpo bruto da requisição (antes de desserializar)
     */
    public boolean isValid(String receivedSignature, String rawPayload) {
        String secret = props.getWebhookSecret();

        if (secret == null || secret.isBlank() || secret.equals("docusign-webhook-secret")) {
            log.warn("DOCUSIGN_WEBHOOK_SECRET não configurado com valor real — validação HMAC desabilitada. " +
                    "Configure antes de ir para produção.");
            return true;
        }

        if (receivedSignature == null || receivedSignature.isBlank()) {
            log.warn("Webhook DocuSign recebido sem header X-DocuSign-Signature-1.");
            return false;
        }

        try {
            String computed = compute(secret, rawPayload);
            boolean valid = constantTimeEquals(receivedSignature, computed);
            if (!valid) {
                log.warn("Assinatura HMAC DocuSign inválida. Recebido: {}, Esperado: {}", receivedSignature, computed);
            }
            return valid;
        } catch (Exception e) {
            log.error("Erro ao validar HMAC do webhook DocuSign", e);
            return false;
        }
    }

    private String compute(String secret, String payload) throws Exception {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
        byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
