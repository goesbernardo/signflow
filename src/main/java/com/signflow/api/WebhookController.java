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
    public ResponseEntity<Void> clicksign(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestHeader(value = "X-Clicksign-Hmac-Sha256", required = false) String hmacHeader,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody String rawPayload) {

        log.info("Webhook ClickSign recebido. Path: {}, HMAC presente: {}, Auth presente: {}", 
                request.getServletPath(), hmacHeader != null, authHeader != null);


        webhookService.process(rawPayload);

        return ResponseEntity.ok().build();
    }
}
