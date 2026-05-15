package com.signflow.infrastructure.provider.docusign.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.webhook.WebhookEventProcessor;
import com.signflow.application.webhook.WebhookHandler;
import com.signflow.infrastructure.provider.docusign.dto.DocuSignWebhookPayloadDTO;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignWebhookEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@Component("docusign")
public class DocuSignWebhookHandlerImpl implements WebhookHandler {

    private final WebhookEventProcessor webhookEventProcessor;
    private final ObjectMapper objectMapper;
    private final DocuSignWebhookEventMapper eventMapper;

    @Override
    public void handle(String provider, String payload) {
        log.info("Processando webhook do provedor DocuSign");

        if (!"docusign".equalsIgnoreCase(provider)) {
            log.warn("Provedor de webhook inesperado no handler DocuSign: {}", provider);
            throw new IllegalArgumentException("Provedor de webhook desconhecido: " + provider);
        }

        try {
            DocuSignWebhookPayloadDTO rootPayload = objectMapper.readValue(payload, DocuSignWebhookPayloadDTO.class);
            log.info("Evento DocuSign recebido: {}", rootPayload.event());
            webhookEventProcessor.process(eventMapper.toNormalizedEvent(rootPayload));
        } catch (Exception e) {
            log.error("Erro ao processar webhook do DocuSign", e);
            throw new RuntimeException("Erro ao processar webhook DocuSign", e);
        }
    }
}
