package com.signflow.api.controller;

import com.signflow.application.service.AuditLogService;
import com.signflow.application.webhook.dto.WebhookReceivedEvent;
import com.signflow.config.KafkaConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook")
@Tag(name = "Webhook Management", description = "Endpoints for managing webhooks")
public class WebhookController {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AuditLogService auditLogService;

    @PostMapping("/{provider}")
    @Operation(summary = "Webhook ",
            description = "Recebe eventos de envelopes e aceites .")
    public ResponseEntity<Void> processWebhook(@PathVariable String provider, @RequestBody String payload) {

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
