package com.signflow.application.webhook.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.api.dto.ClickSignWebhookRootPayloadDTO;
import com.signflow.application.webhook.WebhookHandler;
import com.signflow.service.ClickSignWebhookService;
import com.signflow.service.ClickSignWhatsAppWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component("clicksign")
@Slf4j
public class ClicksignWebhookHandler implements WebhookHandler {

    private final ClickSignWebhookService envelopeWebhookService;
    private final ClickSignWhatsAppWebhookService whatsAppWebhookService;
    private final ObjectMapper objectMapper;

    public ClicksignWebhookHandler(ClickSignWebhookService envelopeWebhookService,
                                   ClickSignWhatsAppWebhookService whatsAppWebhookService,
                                   ObjectMapper objectMapper) {
        this.envelopeWebhookService = envelopeWebhookService;
        this.whatsAppWebhookService = whatsAppWebhookService;
        this.objectMapper = objectMapper;
    }


    @Override
    public void handle(String provider, String payload) {
        routeToService(payload);
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
