package com.signflow.api.controller;

import com.signflow.application.service.AuditLogService;
import com.signflow.application.webhook.dto.WebhookReceivedEvent;
import com.signflow.config.KafkaConfig;
import com.signflow.infrastructure.provider.docusign.security.DocuSignHmacValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/webhook")
@Tag(name = "Webhook Management", description = "Endpoints para recebimento de eventos de providers de assinatura")
public class WebhookController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditLogService auditLogService;

    @Autowired(required = false)
    private DocuSignHmacValidator docuSignHmacValidator;

    @PostMapping("/{provider}")
    @Operation(
            summary = "Receber evento de webhook",
            description = "Recebe eventos de envelopes dos providers (ClickSign, DocuSign, etc.) e publica no Kafka para processamento assíncrono.")
    public ResponseEntity<Void> processWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader(value = "X-DocuSign-Signature-1", required = false) String docuSignSignature) {

        if ("docusign".equalsIgnoreCase(provider) && docuSignHmacValidator != null) {
            if (!docuSignHmacValidator.isValid(docuSignSignature, payload)) {
                log.warn("Webhook DocuSign rejeitado — assinatura HMAC inválida");
                auditLogService.log("WEBHOOK_REJECTED", "PROVIDER", provider,
                        "Assinatura HMAC inválida no webhook DocuSign");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        log.info("Recebendo webhook do provedor {}. Publicando no Kafka.", provider);
        auditLogService.log("WEBHOOK_RECEIVED", "PROVIDER", provider, "Recebido webhook de " + provider);

        WebhookReceivedEvent event = WebhookReceivedEvent.builder()
                .provider(provider)
                .payload(payload)
                .build();

        kafkaTemplate.send(KafkaConfig.WEBHOOK_RECEIVED_TOPIC, provider, event);
        return ResponseEntity.accepted().build();
    }
}
