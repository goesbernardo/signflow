//package com.signflow.application.webhook;
//
//import com.signflow.enums.ProviderSignature;
//import com.signflow.enums.Status;
//import com.signflow.enums.WebhookEventType;
//import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
//import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
//import com.signflow.infrastructure.persistence.entity.SignerEntity;
//import com.signflow.infrastructure.persistence.repository.EnvelopeEventRepository;
//import com.signflow.infrastructure.persistence.repository.EnvelopeRepository;
//import com.signflow.infrastructure.persistence.repository.SignerRepository;
//import org.checkerframework.checker.nullness.qual.AssertNonNullIfNonNull;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.Assert;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//@Transactional
//class WebhookEventProcessorTest {
//
//    @Autowired
//    private WebhookEventProcessor processor;
//
//    @Autowired
//    private EnvelopeRepository envelopeRepository;
//
//    @Autowired
//    private SignerRepository signerRepository;
//
//    @Autowired
//    private EnvelopeEventRepository eventRepository;
//
//    private EnvelopeEntity envelope;
//    private SignerEntity signer;
//
//    @BeforeEach
//    void setUp() {
//        envelope = new EnvelopeEntity();
//        envelope.setExternalId("env-123");
//        envelope.setStatus(Status.PENDING);
//        envelope.setProvider(ProviderSignature.CLICKSIGN);
//        envelope = envelopeRepository.save(envelope);
//
//        signer = new SignerEntity();
//        signer.setExternalId("signer-456");
//        signer.setStatus("PENDING");
//        signer.setEnvelope(envelope);
//        signer = signerRepository.save(signer);
//    }
//
//    @Test
//    void shouldProcessSignedEvent() {
//        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
//                .envelopeExternalId("env-123")
//                .signerExternalId("signer-456")
//                .eventType(WebhookEventType.DOCUMENT_SIGNED)
//                .providerEvent("sign")
//                .providerStatus("running")
//                .occurredAt(LocalDateTime.now())
//                .build();
//
//        processor.process(event);
//
//        EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId("env-123").orElseThrow();
//        assertEquals(Status.PENDING, updatedEnvelope.getStatus());
//
//        SignerEntity updatedSigner = signerRepository.findByExternalId("signer-456").orElseThrow();
//        assertEquals("PENDING", updatedSigner.getStatus());
//
//        List<EnvelopeEventEntity> events = eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc("env-123");
//        assertEquals(Status.PENDING, events.get(0).getPreviousStatus());
//        assertEquals(Status.PENDING, events.get(0).getNewStatus());
//    }
//
//    @Test
//    void shouldProcessRejectedEvent() {
//        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
//                .envelopeExternalId("env-123")
//                .signerExternalId("signer-456")
//                .eventType(WebhookEventType.DOCUMENT_REJECTED)
//                .providerEvent("refusal")
//                .providerStatus("refused")
//                .occurredAt(LocalDateTime.now())
//                .build();
//
//        processor.process(event);
//
//        EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId("env-123").orElseThrow();
//        assertEquals(Status.REFUSED, updatedEnvelope.getStatus());
//
//        SignerEntity updatedSigner = signerRepository.findByExternalId("signer-456").orElseThrow();
//        assertEquals("REFUSED", updatedSigner.getStatus());
//
//        List<EnvelopeEventEntity> events = eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc("env-123");
//        assertFalse(events.isEmpty());
//    }
//
//    @Test
//    void shouldProcessCompletedEvent() {
//        envelope.setStatus(Status.ACTIVE);
//        envelopeRepository.save(envelope);
//
//        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
//                .envelopeExternalId("env-123")
//                .eventType(WebhookEventType.DOCUMENT_COMPLETED)
//                .providerEvent("document_closed")
//                .providerStatus("closed")
//                .occurredAt(LocalDateTime.now())
//                .build();
//
//        processor.process(event);
//
//        EnvelopeEntity updatedEnvelope = envelopeRepository.findByExternalId("env-123").orElseThrow();
//        assertEquals(Status.CLOSED, updatedEnvelope.getStatus());
//
//        List<EnvelopeEventEntity> events = eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc("env-123");
//        assertFalse(events.isEmpty());
//
//    }
//}
