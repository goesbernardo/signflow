package com.signflow.infrastructure.provider.clicksign.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.webhook.WebhookEventProcessor;
import com.signflow.application.webhook.WebhookHandler;
import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO;
import com.signflow.infrastructure.provider.clicksign.mapper.ClicksignWebhookEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Component("clicksign")
public class ClickSignWebhookHandlerImpl implements WebhookHandler {

    private final WebhookEventProcessor webhookEventProcessor;
    private final ObjectMapper objectMapper;
    private final ClicksignWebhookEventMapper eventMapper;


    @Override
    public void handle(String provider, String payload) {
        log.info("Encaminhando webhook do provedor: {}", provider);

        if ("clicksign".equalsIgnoreCase(provider)) {
            try {
                ClickSignWebhookRootPayloadDTO rootPayload = objectMapper.readValue(payload, ClickSignWebhookRootPayloadDTO.class);
                webhookEventProcessor.process(eventMapper.toNormalizedEvent(rootPayload));
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
