package com.signflow.adapter.clicksign.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.adapter.clicksign.dto.WebhookAttributesDTO;
import com.signflow.api.dto.ClickSignWebhookRootPayloadDTO;
import com.signflow.enums.Status;
import com.signflow.persistence.EnvelopeEntity;
import com.signflow.persistence.EnvelopeEventEntity;
import com.signflow.persistence.EnvelopeEventRepository;
import com.signflow.persistence.SignatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickSignWebhookService {

    // Mesmo mapa do ClickSignMapper — fonte única de verdade
    private static final Map<String, Status> STATUS_MAP = Map.of(
            "running",   Status.ACTIVE,
            "completed", Status.CLOSED,
            "canceled",  Status.CANCELED,
            "draft",     Status.DRAFT
    );

    private final SignatureRepository envelopeRepository;
    private final EnvelopeEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    /**
     * Processa o payload em background após o controller já ter retornado 200.
     *
     * @param payload objeto já desserializado pelo controller
     */
    @Async
    @Transactional
    public void process(ClickSignWebhookRootPayloadDTO payload) {
        log.info("Processando webhook ClickSign...");

        if (payload.data() == null) {
            log.warn("Webhook ClickSign recebido sem campo 'data'. Ignorando.");
            return;
        }

        String externalId = payload.data().id();
        if (externalId == null || externalId.isBlank()) {
            log.warn("Webhook ClickSign sem externalId. Ignorando.");
            return;
        }

        String rawStatus = Optional.ofNullable(payload.data().attributes())
                .map(WebhookAttributesDTO::status)
                .map(String::toLowerCase)
                .orElse(null);

        if (rawStatus == null) {
            log.warn("Webhook ClickSign sem status para envelope {}. Ignorando.", externalId);
            return;
        }

        Status newStatus = STATUS_MAP.get(rawStatus);

        Optional<EnvelopeEntity> entityOpt = envelopeRepository.findByExternalId(externalId);
        if (entityOpt.isEmpty()) {
            log.warn("Webhook recebido para envelope desconhecido: {}. " +
                    "Pode ter sido criado fora do SignFlow.", externalId);
            return;
        }

        EnvelopeEntity entity = entityOpt.get();
        Status previousStatus = entity.getStatus();
        
        entity.setStatus(newStatus != null ? newStatus : entity.getStatus());
        entity.setProviderStatus(rawStatus);
        envelopeRepository.save(entity);

        EnvelopeEventEntity event = new EnvelopeEventEntity();
        event.setEnvelope(entity);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setProviderStatus(rawStatus);
        event.setSource("WEBHOOK");
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);

        log.info("Envelope {} atualizado via webhook. Status interno: {} | Status Provedor: {}", 
                externalId, newStatus, rawStatus);
    }
}
