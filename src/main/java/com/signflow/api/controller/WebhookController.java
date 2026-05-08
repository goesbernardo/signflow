package com.signflow.api.controller;

import com.signflow.application.webhook.WebhookHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/webhook")
@Tag(name = "Webhook Management", description = "Endpoints for managing webhooks")
public class WebhookController {

    private final WebhookHandler webhookHandler;


    @PostMapping("/{provider}")
    @Operation(summary = "Webhook ",
            description = "Recebe eventos de envelopes e aceites .")
    public ResponseEntity<Void> processWebhook(@PathVariable String provider, @RequestBody String payload) {

        log.info("Recebendo webhook : {}", payload);
        webhookHandler.handle(provider, payload);
        return ResponseEntity.ok().build();
    }

}
