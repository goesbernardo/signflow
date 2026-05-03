package com.signflow.api;

import com.signflow.adapter.clicksign.webhook.ClickSignHmacValidator;
import com.signflow.adapter.clicksign.webhook.ClickSignWebhookService;
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

    private final ClickSignHmacValidator hmacValidator;
    private final ClickSignWebhookService webhookService;

    @PostMapping("/clicksign")
    @Operation(
            summary = "Webhook ClickSign",
            description = "Recebe notificações de eventos de assinatura da ClickSign. " + "Requer validação HMAC via header X-Clicksign-Hmac-Sha256.")
    public ResponseEntity<Void> clicksign(@RequestHeader(value = "X-Clicksign-Hmac-Sha256", required = false) String hmacHeader, @RequestBody String rawPayload) {

        log.info("Webhook ClickSign recebido. HMAC presente: {}", hmacHeader != null);

        // 1. Validar assinatura HMAC — rejeitar imediatamente se inválida
        if (!hmacValidator.isValid(hmacHeader, rawPayload)) {
            log.warn("Webhook ClickSign rejeitado: assinatura HMAC inválida.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. Retornar 200 imediatamente — ClickSign considera timeout > 5s como falha
        // 3. Processar em background via @Async
        webhookService.process(rawPayload);

        return ResponseEntity.ok().build();
    }
}
