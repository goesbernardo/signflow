package com.signflow.application.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.service.ClickSignWebhookService;
import com.signflow.application.webhook.WebhookHandler;
import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookHandlerImpl implements WebhookHandler {

    private final ClickSignWebhookService clickSignWebhookService;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(String provider, String payload) {
        log.info("Encaminhando webhook do provedor: {}", provider);

        if ("clicksign".equalsIgnoreCase(provider)) {
            try {
                ClickSignWebhookRootPayloadDTO rootPayload = objectMapper.readValue(payload, ClickSignWebhookRootPayloadDTO.class);
                clickSignWebhookService.process(rootPayload);
            } catch (Exception e) {
                log.error("Erro ao desserializar webhook da ClickSign", e);
                throw new RuntimeException("Erro ao processar webhook ClickSign", e);
            }
        } else {
            log.warn("Provedor de webhook não suportado: {}", provider);
            throw new IllegalArgumentException("Provedor de webhook desconhecido: " + provider);
        }
    }
}
