package com.signflow.application.service;

import com.signflow.infrastructure.provider.clicksign.dto.WebhookAttributesDTO;
import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
import com.signflow.infrastructure.persistence.entity.SignerEntity;
import com.signflow.enums.ClickSignWebhookEvent;
import com.signflow.enums.Status;
import com.signflow.infrastructure.persistence.repository.EnvelopeEventRepository;
import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
import com.signflow.infrastructure.persistence.repository.SignerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.signflow.infrastructure.provider.clicksign.dto.ClickSignWebhookRootPayloadDTO.WebhookDataWrapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickSignWebhookService {

        private final EnvelopeRepository envelopeRepository;
        private final SignerRepository signerRepository;
        private final EnvelopeEventRepository eventRepository;

        // Mapeamento do status do provedor → status interno
        private static final Map<String, Status> STATUS_MAP = Map.of(
                "running",   Status.ACTIVE,
                "completed", Status.CLOSED,
                "canceled",  Status.CANCELED,
                "draft",     Status.DRAFT
        );

        // ── Entry point ───────────────────────────────────────────────────────────

        @Async
        @Transactional
        public void process(ClickSignWebhookRootPayloadDTO payload) {
            log.info("Processando webhook ClickSign...");

            if (payload.data() == null || payload.data().id() == null) {
                log.warn("Webhook recebido sem campo 'data' ou 'id'. Ignorando.");
                return;
            }

            String envelopeExternalId = payload.data().id();

            String rawStatus = Optional.ofNullable(payload.data().attributes())
                    .map(WebhookAttributesDTO::status)
                    .orElse(null);

            String rawEvent = Optional.ofNullable(payload.event())
                    .map(ClickSignWebhookRootPayloadDTO.EventWrapper::name)
                    .orElse(null);

            ClickSignWebhookEvent event = ClickSignWebhookEvent.fromValue(rawEvent);

            log.info("Webhook recebido — envelope: {}, evento: {}, status do provedor: {}",
                    envelopeExternalId, event.getValue(), rawStatus);

            envelopeRepository.findByExternalId(envelopeExternalId).ifPresentOrElse(
                    envelope -> route(envelope, payload, event, rawStatus, rawEvent),
                    () -> log.warn("Webhook recebido para envelope desconhecido: {}. " +
                            "Pode ter sido criado fora do SignFlow.", envelopeExternalId)
            );
        }

        private void route(EnvelopeEntity envelope, ClickSignWebhookRootPayloadDTO payload, ClickSignWebhookEvent event, String rawStatus, String rawEvent) {
            switch (event) {

                case SIGN ->
                        handleSign(envelope, payload, rawStatus, rawEvent);

                case REFUSAL ->
                        handleRefusal(envelope, payload, rawStatus, rawEvent);

                case CLOSE, AUTO_CLOSE, DOCUMENT_CLOSED ->
                        handleStatusChange(envelope, Status.CLOSED, rawStatus, rawEvent);

                case CANCEL ->
                        handleStatusChange(envelope, Status.CANCELED, rawStatus, rawEvent);

                case DEADLINE ->
                        handleStatusChange(envelope, Status.EXPIRED, rawStatus, rawEvent);

                case ADD_SIGNER ->
                        log.info("Signatário adicionado ao envelope {} (sem persistência adicional).",
                                envelope.getExternalId());

                case REMOVE_SIGNER ->
                        log.info("Signatário removido do envelope {} (sem persistência adicional).",
                                envelope.getExternalId());

                case UPDATE_DEADLINE, UPDATE_AUTO_CLOSE, UPDATE_LOCALE ->
                        log.info("Configuração do envelope {} atualizada via webhook: {}",
                                envelope.getExternalId(), event.getValue());

                case ATTEMPTS_BY_WHATSAPP_EXCEEDED,
                     ATTEMPTS_BY_LIVENESS_OR_FACEMATCH_EXCEEDED,
                     LIVENESS_REFUSED,
                     FACEMATCH_REFUSED,
                     BIOMETRIC_REFUSED,
                     DOCUMENTSCOPY_REFUSED,
                     OCR_REFUSED ->
                        handleAuthFailure(envelope, event, rawStatus, rawEvent);

                case UNKNOWN ->
                        log.warn("Evento desconhecido recebido para envelope {}: '{}'",
                                envelope.getExternalId(), rawEvent);

                default ->
                        log.info("Evento '{}' não requer persistência específica para envelope {}.",
                                event.getValue(), envelope.getExternalId());
            }
        }


        /**
         * Evento: sign
         * Marca o signatário como SIGNED e atualiza o status do envelope.
         */
        private void handleSign(EnvelopeEntity envelope, ClickSignWebhookRootPayloadDTO payload, String rawStatus, String rawEvent) {

            SignerEntity signer = resolveSignerFromPayload(payload);

            if (signer != null) {
                signer.setStatus("SIGNED");
                signer.setSignedAt(LocalDateTime.now());
                signerRepository.save(signer);
                log.info("Signatário {} marcado como SIGNED no envelope {}.",
                        signer.getExternalId(), envelope.getExternalId());
            } else {
                log.warn("Evento 'sign' recebido mas signatário não identificado no payload. " + "Envelope: {}", envelope.getExternalId());
            }

            Status newStatus = STATUS_MAP.getOrDefault(rawStatus, envelope.getStatus());
            saveEvent(envelope, newStatus, rawStatus, rawEvent, signer);
        }

        /**
         * Evento: refusal
         * Marca o signatário como REFUSED e o envelope como REFUSED.
         */
        private void handleRefusal(EnvelopeEntity envelope, ClickSignWebhookRootPayloadDTO payload, String rawStatus, String rawEvent) {

            SignerEntity signer = resolveSignerFromPayload(payload);

            if (signer != null) {
                signer.setStatus("REFUSED");
                signerRepository.save(signer);
                log.info("Signatário {} marcou como REFUSED no envelope {}.", signer.getExternalId(), envelope.getExternalId());
            }
            saveEvent(envelope, Status.REFUSED, rawStatus, rawEvent, signer);
        }

        /**
         * Eventos: close, auto_close, document_closed, cancel, deadline
         * Atualiza apenas o status do envelope — sem signatário vinculado.
         */
        private void handleStatusChange(EnvelopeEntity envelope, Status newStatus, String rawStatus, String rawEvent) {
            saveEvent(envelope, newStatus, rawStatus, rawEvent, null);
        }

        /**
         * Eventos de falha de autenticação.
         * Registra o evento na timeline sem alterar o status do envelope.
         */
        private void handleAuthFailure(EnvelopeEntity envelope, ClickSignWebhookEvent event, String rawStatus, String rawEvent) {
            log.warn("Falha de autenticação no envelope {}: {}", envelope.getExternalId(), event.getValue());

            // Registra evento sem mudar status do envelope
            EnvelopeEventEntity eventEntity = new EnvelopeEventEntity();
            eventEntity.setEnvelope(envelope);
            eventEntity.setSigner(null);
            eventEntity.setPreviousStatus(envelope.getStatus());
            eventEntity.setNewStatus(envelope.getStatus()); // sem mudança
            eventEntity.setProviderStatus(rawStatus);
            eventEntity.setProviderEvent(rawEvent);
            eventEntity.setSource("WEBHOOK");
            eventEntity.setOccurredAt(LocalDateTime.now());
            eventRepository.save(eventEntity);
        }

        /**
         * Atualiza o status do envelope e persiste o evento na timeline.
         */
        private void saveEvent(EnvelopeEntity envelope, Status newStatus, String rawStatus, String rawEvent, SignerEntity signer) {

            Status previousStatus = envelope.getStatus();

            // Atualizar envelope
            envelope.setStatus(newStatus);
            envelope.setProviderStatus(rawStatus);
            envelopeRepository.save(envelope);

            // Registrar evento na timeline
            EnvelopeEventEntity eventEntity = new EnvelopeEventEntity();
            eventEntity.setEnvelope(envelope);
            eventEntity.setSigner(signer);
            eventEntity.setPreviousStatus(previousStatus);
            eventEntity.setNewStatus(newStatus);
            eventEntity.setProviderStatus(rawStatus);
            eventEntity.setProviderEvent(rawEvent);
            eventEntity.setSource("WEBHOOK");
            eventEntity.setOccurredAt(LocalDateTime.now());
            eventRepository.save(eventEntity);

            log.info("Evento persistido — envelope: {}, {} → {}, evento: '{}', signer: {}", envelope.getExternalId(), previousStatus, newStatus, rawEvent, signer != null ? signer.getExternalId() : "N/A");
        }

        // ── Helper ────────────────────────────────────────────────────────────────

        /**
         * Tenta identificar o signatário a partir do payload do webhook.
         * O externalId do signer vem em payload.data quando type = "signers".
         */
        private SignerEntity resolveSignerFromPayload(ClickSignWebhookRootPayloadDTO payload) {
            if (payload.data() == null) return null;

            String type = payload.data().type();
            String externalId = payload.data().id();

            if ("signers".equalsIgnoreCase(type) && externalId != null && !externalId.isBlank()) {
                return signerRepository.findByExternalId(externalId).orElseGet(() -> {
                    log.warn("Signatário com externalId {} não encontrado no banco.", externalId);
                    return null;
                });
            }
            return null;
        }
}
