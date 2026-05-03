package com.signflow.adapter.clicksign.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.adapter.clicksign.dto.ClickSignWebhookPayloadDTO;
import com.signflow.adapter.clicksign.dto.WebhookAttributesDTO;
import com.signflow.adapter.clicksign.dto.WebhookData;
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
     * @param rawPayload corpo bruto recebido da ClickSign
     */
    @Async
    @Transactional
    public void process(String rawPayload) {
        log.info("Processando webhook ClickSign...");

        ClickSignWebhookPayloadDTO payload;
        try {
            payload = objectMapper.readValue(rawPayload, ClickSignWebhookPayloadDTO.class);
        } catch (Exception e) {
            log.error("Falha ao desserializar payload do webhook ClickSign: {}", rawPayload, e);
            return;
        }

        if (payload.getData() == null) {
            log.warn("Webhook ClickSign recebido sem campo 'data'. Ignorando.");
            return;
        }

        String externalId = payload.getData().getId();
        if (externalId == null || externalId.isBlank()) {
            log.warn("Webhook ClickSign sem externalId. Ignorando.");
            return;
        }

        // Mapear status da ClickSign para o domínio
        String rawStatus = Optional.ofNullable(payload.getData().getAttributes())
                .map(WebhookAttributesDTO::getStatus)
                .map(String::toLowerCase)
                .orElse(null);

        if (rawStatus == null) {
            log.warn("Webhook ClickSign sem status para envelope {}. Ignorando.", externalId);
            return;
        }

        Status newStatus = STATUS_MAP.get(rawStatus);
        if (newStatus == null) {
            log.warn("Status desconhecido recebido via webhook: '{}' para envelope {}. Ignorando.",
                    rawStatus, externalId);
            return;
        }

        // Buscar envelope no banco e atualizar
        Optional<EnvelopeEntity> entityOpt = envelopeRepository.findByExternalId(externalId);
        if (entityOpt.isEmpty()) {
            log.warn("Webhook recebido para envelope desconhecido: {}. " +
                    "Pode ter sido criado fora do SignFlow.", externalId);
            return;
        }

        EnvelopeEntity entity = entityOpt.get();
        Status previousStatus = entity.getStatus();

        if (previousStatus == newStatus) {
            log.info("Envelope {} já está com status {}. Nenhuma atualização necessária.",
                    externalId, newStatus);
            return;
        }

        // Atualizar status e gravar evento de auditoria
        entity.setStatus(newStatus);
        envelopeRepository.save(entity);

        EnvelopeEventEntity event = new EnvelopeEventEntity();
        event.setEnvelope(entity);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setSource("WEBHOOK");
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);

        log.info("Envelope {} atualizado via webhook: {} → {}", externalId, previousStatus, newStatus);
    }
}
