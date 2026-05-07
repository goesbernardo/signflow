package com.signflow.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.adapter.clicksign.webhook.ClickSignHmacValidator;
import com.signflow.adapter.clicksign.webhook.ClickSignWebhookService;
import com.signflow.adapter.clicksign.webhook.ClickSignWhatsAppWebhookService;
import com.signflow.api.dto.ClickSignWebhookRootPayloadDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhook")
@RequiredArgsConstructor
@Tag(name = "Webhook Management", description = "Endpoints for managing webhooks")
public class WebhookController {

    private final ClickSignWebhookService         envelopeWebhookService;
    private final ClickSignWhatsAppWebhookService whatsAppWebhookService;
    private final ClickSignHmacValidator          hmacValidator;
    private final ObjectMapper                    objectMapper;

    @PostMapping("/clicksign")
    @Operation(summary = "Webhook ClickSign",
            description = "Recebe eventos de envelopes e aceites via WhatsApp da ClickSign.")
    public ResponseEntity<Void> clicksign(@RequestHeader(value = "X-Clicksign-Hmac-Sha256", required = false) String hmac,
                                          @RequestBody String rawPayload) {

        log.info("Recebendo webhook ClickSign");

        // Em desenvolvimento com ngrok, a ClickSign sandbox pode não enviar HMAC
        // Só valida se o header estiver presente
        if (hmac != null && !hmacValidator.isValid(hmac, rawPayload)) {
            log.warn("Falha na validação HMAC para o webhook ClickSign.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        routeToService(rawPayload);
           return ResponseEntity.ok().build();
    }

    private void routeToService(String rawPayload) {
        try {
            ClickSignWebhookRootPayloadDTO root = objectMapper.readValue(rawPayload, ClickSignWebhookRootPayloadDTO.class);

            // 1. Formato JSON:API — envelopes ou outros objetos que usam "data"
            if (root.data() != null && root.data().type() != null) {
                String type = root.data().type();
                log.info("Roteando webhook — formato JSON:API, type: {}", type);

                if ("envelopes".equalsIgnoreCase(type)) {
                    envelopeWebhookService.process(root);
                } else {
                    log.warn("Tipo de objeto JSON:API desconhecido: {}. Ignorando.", type);
                }
                return;
            }

            // 2. Formato legado ou específico — eventos que usam "event"
            if (root.event() != null && root.event().name() != null) {
                String eventName = root.event().name();
                log.info("Roteando webhook — formato legacy/event, event: {}", eventName);

                if (eventName.startsWith("acceptance_term")) {
                    whatsAppWebhookService.processLegacy(root);
                } else {
                    log.warn("Evento legado desconhecido: {}. Ignorando.", eventName);
                }
                return;
            }

            log.warn("Payload de webhook sem estrutura reconhecível (data.type ou event.name). Ignorando.");

        } catch (Exception e) {
            log.error("Erro ao desserializar ou rotear webhook ClickSign: {}", e.getMessage());
        }
    }
}
