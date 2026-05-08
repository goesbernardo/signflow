package com.signflow.application.service;

import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
import com.signflow.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Processa os eventos de webhook do Aceite via WhatsApp recebidos da ClickSign.
 * <p>
 * Eventos tratados (type: acceptance_term_whatsapps):
 * - acceptance_term_enqueued → na fila para envio
 * - acceptance_term_sent → enviado ao destinatário
 * - acceptance_term_completed → aceite confirmado
 * - acceptance_term_refused → aceite recusado
 * - acceptance_term_expired → expirado
 * - acceptance_term_canceled → cancelado
 * - acceptance_term_error → erro no envio
 */

import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
import com.signflow.infrastructure.persistence.repository.EnvelopeEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppWebhookService {

    private final EnvelopeRepository envelopeRepository;
    private final EnvelopeEventRepository eventRepository;

    /**
     * Processa o payload no formato legado já deserializado pelo controller.
     */
    @Async("webhookExecutor")
    @Transactional
    public void processLegacy(ClickSignWebhookRootPayloadDTO root) {
        String eventName = root.event().name();
        ClickSignWebhookRootPayloadDTO.AcceptanceData acceptance = root.acceptance();

        if (acceptance == null) {
            log.warn("Webhook WhatsApp sem campo 'acceptance'. Ignorando.");
            return;
        }

        String externalId = acceptance.key();
        String signerName = acceptance.signerName();
        String status     = acceptance.status();

        log.info("Processando evento WhatsApp: event={}, id={}, status={}, signatário={}",
                eventName, externalId, status, signerName);

        switch (eventName) {
            case "acceptance_term_enqueued" ->
                    log.info("Aceite WhatsApp {} na fila de envio — signatário: {}", externalId, signerName);

            case "acceptance_term_sent" ->
                    log.info("Aceite WhatsApp {} enviado ao signatário: {} ({})",
                            externalId, signerName, acceptance.signerPhone());

            case "acceptance_term_completed" -> {
                log.info("Aceite WhatsApp {} CONFIRMADO pelo signatário: {}", externalId, signerName);
                onAcceptanceCompleted(externalId, acceptance);
            }

            case "acceptance_term_refused" -> {
                log.warn("Aceite WhatsApp {} RECUSADO pelo signatário: {}", externalId, signerName);
                onAcceptanceRefused(externalId, acceptance);
            }

            case "acceptance_term_expired" ->
                    log.warn("Aceite WhatsApp {} expirado — signatário: {}", externalId, signerName);

            case "acceptance_term_canceled" ->
                    log.warn("Aceite WhatsApp {} cancelado — signatário: {}", externalId, signerName);

            case "acceptance_term_error" ->
                    log.error("Erro no Aceite WhatsApp {} — signatário: {}", externalId, signerName);

            default ->
                    log.warn("Evento WhatsApp desconhecido: '{}' para aceite {}. Ignorando.", eventName, externalId);
        }
    }

    private void onAcceptanceCompleted(String externalId, ClickSignWebhookRootPayloadDTO.AcceptanceData acceptance) {
        log.info("✓ Aceite {} confirmado por {} ({}) — título: {}", externalId, acceptance.signerName(), acceptance.signerPhone(), acceptance.name());
        saveEvent(externalId, acceptance, Status.CLOSED);
    }

    private void onAcceptanceRefused(String externalId, ClickSignWebhookRootPayloadDTO.AcceptanceData acceptance) {
        log.warn("✗ Aceite {} recusado por {} ({}) — título: {}", externalId, acceptance.signerName(), acceptance.signerPhone(), acceptance.name());
        saveEvent(externalId, acceptance, Status.CANCELED);
    }

    private void saveEvent(String externalId, ClickSignWebhookRootPayloadDTO.AcceptanceData acceptance, Status newStatus) {
        Optional<EnvelopeEntity> entityOpt = envelopeRepository.findByExternalId(externalId);
        if (entityOpt.isEmpty()) {
            log.warn("Tentativa de salvar evento para aceite desconhecido: {}", externalId);
            return;
        }

        EnvelopeEntity entity = entityOpt.get();
        Status previousStatus = entity.getStatus();
        String rawStatus = acceptance.status();

        entity.setStatus(newStatus);
        entity.setProviderStatus(rawStatus);
        envelopeRepository.save(entity);

        EnvelopeEventEntity event = new EnvelopeEventEntity();
        event.setEnvelope(entity);
        event.setPreviousStatus(previousStatus);
        event.setNewStatus(newStatus);
        event.setProviderStatus(rawStatus);
        event.setSource("WEBHOOK_WHATSAPP");
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);

        log.info("Evento de aceite WhatsApp salvo para envelope {}. Novo status: {}", externalId, newStatus);
    }
}
