package com.signflow.application.webhook;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.enums.WebhookEventType;
import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
import com.signflow.infrastructure.persistence.entity.SignerEntity;
import com.signflow.infrastructure.persistence.repository.EnvelopeEventRepository;
import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
import com.signflow.infrastructure.persistence.repository.SignerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class WebhookEventProcessorIntegrationTest {

    @Autowired
    private WebhookEventProcessor processor;

    @Autowired
    private EnvelopeRepository envelopeRepository;

    @Autowired
    private SignerRepository signerRepository;

    @Autowired
    private EnvelopeEventRepository eventRepository;

    private EnvelopeEntity envelope;
    private SignerEntity signer;

    @BeforeEach
    @Transactional
    void setUp() {
        try {
            eventRepository.deleteAll();
            signerRepository.deleteAll();
            envelopeRepository.deleteAll();
        } catch (Exception e) {
            // Ignorar erros de deleção se as tabelas já estiverem limpas ou em estado inconsistente
        }

        envelope = new EnvelopeEntity();
        envelope.setExternalId("env-123-" + System.currentTimeMillis());
        envelope.setStatus(Status.PENDING);
        envelope.setProvider(ProviderSignature.CLICKSIGN);
        envelope = envelopeRepository.save(envelope);

        signer = new SignerEntity();
        signer.setExternalId("signer-456-" + System.currentTimeMillis());
        signer.setStatus("PENDING");
        signer.setEnvelope(envelope);
        signer = signerRepository.save(signer);
    }

    @Test
    void shouldProcessSignedEvent() {
        String envId = envelope.getExternalId();
        String sId = signer.getExternalId();
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId(envId)
                .signerExternalId(sId)
                .eventType(WebhookEventType.DOCUMENT_SIGNED)
                .providerEvent("sign")
                .providerStatus("running")
                .occurredAt(LocalDateTime.now())
                .build();

        processor.process(event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId(envId).orElseThrow();
            assertEquals(Status.ACTIVE, updatedEnvelope.getStatus());

            SignerEntity updatedSigner = signerRepository.findByExternalId(sId).orElseThrow();
            assertEquals("SIGNED", updatedSigner.getStatus());
        });
    }

    @Test
    void shouldProcessCanceledEvent() {
        String envId = envelope.getExternalId();
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId(envId)
                .eventType(WebhookEventType.DOCUMENT_CANCELED)
                .providerEvent("cancel")
                .providerStatus("canceled")
                .occurredAt(LocalDateTime.now())
                .build();

        processor.process(event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId(envId).orElseThrow();
            assertEquals(Status.CANCELED, updatedEnvelope.getStatus());
        });
    }

    @Test
    void shouldProcessExpiredEvent() {
        String envId = envelope.getExternalId();
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId(envId)
                .eventType(WebhookEventType.DOCUMENT_EXPIRED)
                .providerEvent("deadline")
                .providerStatus("expired")
                .occurredAt(LocalDateTime.now())
                .build();

        processor.process(event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId(envId).orElseThrow();
            assertEquals(Status.EXPIRED, updatedEnvelope.getStatus());
        });
    }

    @Test
    void shouldProcessBiometricRefusedEvent() {
        String envId = envelope.getExternalId();
        String sId = signer.getExternalId();
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId(envId)
                .signerExternalId(sId)
                .eventType(WebhookEventType.BIOMETRIC_REFUSED)
                .providerEvent("biometric_refused")
                .providerStatus("running")
                .occurredAt(LocalDateTime.now())
                .build();

        processor.process(event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            SignerEntity updatedSigner = signerRepository.findByExternalId(sId).orElseThrow();
            assertEquals("BIOMETRIC_REFUSED", updatedSigner.getStatus());

            List<EnvelopeEventEntity> events = eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc(envId);
            assertFalse(events.isEmpty());
        });
    }

    @Test
    void shouldProcessAuditOnlyEvents() {
        String envId = envelope.getExternalId();
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId(envId)
                .eventType(WebhookEventType.DOCUMENT_VIEWED)
                .providerEvent("view")
                .providerStatus("running")
                .occurredAt(LocalDateTime.now())
                .build();

        processor.process(event);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<EnvelopeEventEntity> events = eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc(envId);
            assertFalse(events.isEmpty());
            assertEquals("view", events.get(0).getProviderEvent());
        });
    }
}
