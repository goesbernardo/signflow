package com.signflow.application.webhook;

import com.signflow.enums.Status;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
import com.signflow.infrastructure.persistence.repository.EnvelopeEventRepository;
import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
import com.signflow.infrastructure.persistence.repository.SignerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookEventProcessor {

    private final EnvelopeRepository envelopeRepository;
    private final SignerRepository signerRepository;
    private final EnvelopeEventRepository eventRepository;

    @Async("webhookExecutor")
    @Transactional
    public void process(NormalizedWebhookEvent event) {
        log.info("Processando evento {} para o envelope {}", event.getEventType(), event.getEnvelopeExternalId());

        switch (event.getEventType()) {

            case DOCUMENT_SIGNED:
                handleSigned(event);
                break;

            case DOCUMENT_REJECTED:
                handleRejected(event);
                break;

            case DOCUMENT_COMPLETED:
                handleCompleted(event);
                break;

            case DOCUMENT_CANCELED:
                handleCanceled(event);
                break;

            case DOCUMENT_EXPIRED:
                handleExpired(event);
                break;

            case BIOMETRIC_REFUSED:
                handleBiometricRefused(event);
                break;

            case DOCUMENT_CREATED:
            case DOCUMENT_SENT:
            case DOCUMENT_VIEWED:
            case SIGNER_ADDED:
            case SIGNER_REMOVED:
                handleAuditOnlyEvent(event);
                break;

            default:
                log.warn(
                        "Unhandled event: {}",
                        event.getEventType()
                );
        }
    }

    private void handleSigned(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            Status previousStatus = envelope.getStatus();

            // Atualizar Signatário se identificado
            if (event.getSignerExternalId() != null) {
                signerRepository.findByExternalId(event.getSignerExternalId()).ifPresent(signer -> {
                    signer.setStatus("SIGNED");
                    signer.setSignedAt(event.getOccurredAt());
                    signerRepository.save(signer);
                    log.debug("Signatário {} marcado como SIGNED", event.getSignerExternalId());
                });
            }

            // O envelope continua ACTIVE enquanto aguarda outras assinaturas,
            // mas podemos garantir que ele não esteja mais como PENDING/DRAFT
            if (envelope.getStatus() == Status.PENDING || envelope.getStatus() == Status.DRAFT) {
                envelope.setStatus(Status.ACTIVE);
                envelope.setProviderStatus(event.getProviderStatus());
                envelopeRepository.save(envelope);
            }

            saveEvent(envelope, previousStatus, envelope.getStatus(), event);
        });
    }

    private void handleRejected(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            Status previousStatus = envelope.getStatus();

            // Marcar signatário que recusou, se houver
            if (event.getSignerExternalId() != null) {
                signerRepository.findByExternalId(event.getSignerExternalId()).ifPresent(signer -> {
                    signer.setStatus("REFUSED");
                    signerRepository.save(signer);
                });
            }

            envelope.setStatus(Status.REFUSED);
            envelope.setProviderStatus(event.getProviderStatus());
            envelopeRepository.save(envelope);

            saveEvent(envelope, previousStatus, Status.REFUSED, event);
        });
    }

    private void handleCanceled(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            Status previousStatus = envelope.getStatus();
            envelope.setStatus(Status.CANCELED);
            envelope.setProviderStatus(event.getProviderStatus());
            envelopeRepository.save(envelope);
            saveEvent(envelope, previousStatus, Status.CANCELED, event);
        });
    }

    private void handleExpired(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            Status previousStatus = envelope.getStatus();
            envelope.setStatus(Status.EXPIRED);
            envelope.setProviderStatus(event.getProviderStatus());
            envelopeRepository.save(envelope);
            saveEvent(envelope, previousStatus, Status.EXPIRED, event);
        });
    }

    private void handleBiometricRefused(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            if (event.getSignerExternalId() != null) {
                signerRepository.findByExternalId(event.getSignerExternalId()).ifPresent(signer -> {
                    signer.setStatus("BIOMETRIC_REFUSED");
                    signerRepository.save(signer);
                });
            }
            saveEvent(envelope, envelope.getStatus(), envelope.getStatus(), event);
        });
    }

    private void handleAuditOnlyEvent(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            saveEvent(envelope, envelope.getStatus(), envelope.getStatus(), event);
        });
    }

    private void handleCompleted(NormalizedWebhookEvent event) {
        envelopeRepository.findByExternalId(event.getEnvelopeExternalId()).ifPresent(envelope -> {
            Status previousStatus = envelope.getStatus();

            envelope.setStatus(Status.CLOSED);
            envelope.setProviderStatus(event.getProviderStatus());
            envelopeRepository.save(envelope);

            saveEvent(envelope, previousStatus, Status.CLOSED, event);
        });
    }

    private void saveEvent(EnvelopeEntity envelope, Status previous, Status next, NormalizedWebhookEvent event) {
        EnvelopeEventEntity eventEntity = new EnvelopeEventEntity();
        eventEntity.setEnvelope(envelope);
        eventEntity.setPreviousStatus(previous);
        eventEntity.setNewStatus(next);
        eventEntity.setProviderEvent(event.getProviderEvent());
        eventEntity.setProviderStatus(event.getProviderStatus());
        eventEntity.setSource("WEBHOOK");
        eventEntity.setOccurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : LocalDateTime.now());

        if (event.getSignerExternalId() != null) {
            signerRepository.findByExternalId(event.getSignerExternalId()).ifPresent(eventEntity::setSigner);
        }

        eventRepository.save(eventEntity);
        log.info("Evento {} registrado para envelope {}", event.getProviderEvent(), envelope.getExternalId());
    }
}
