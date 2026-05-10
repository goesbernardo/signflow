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
import com.signflow.config.KafkaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookEventProcessorTest {

    @InjectMocks
    private WebhookEventProcessor processor;

    @Mock
    private EnvelopeRepository envelopeRepository;

    @Mock
    private SignerRepository signerRepository;

    @Mock
    private EnvelopeEventRepository eventRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private EnvelopeEntity envelope;
    private SignerEntity signer;

    @BeforeEach
    void setUp() {
        envelope = new EnvelopeEntity();
        envelope.setExternalId("env-123");
        envelope.setStatus(Status.PENDING);
        envelope.setProvider(ProviderSignature.CLICKSIGN);

        signer = new SignerEntity();
        signer.setExternalId("signer-456");
        signer.setStatus("PENDING");
        signer.setEnvelope(envelope);
    }

    @Test
    void shouldProcessSignedEvent() {
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-123")
                .signerExternalId("signer-456")
                .eventType(WebhookEventType.DOCUMENT_SIGNED)
                .providerEvent("sign")
                .providerStatus("running")
                .occurredAt(LocalDateTime.now())
                .build();

        when(envelopeRepository.findByExternalId("env-123")).thenReturn(Optional.of(envelope));
        when(signerRepository.findByExternalId("signer-456")).thenReturn(Optional.of(signer));

        processor.process(event);

        assertEquals(Status.ACTIVE, envelope.getStatus());
        assertEquals("SIGNED", signer.getStatus());
        
        verify(envelopeRepository).save(envelope);
        verify(signerRepository).save(signer);
        verify(eventRepository).save(any(EnvelopeEventEntity.class));
        verify(kafkaTemplate).send(eq(KafkaConfig.ENVELOPE_EVENTS_TOPIC), eq(envelope.getExternalId()), eq(event));
    }

    @Test
    void shouldProcessRejectedEvent() {
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-123")
                .signerExternalId("signer-456")
                .eventType(WebhookEventType.DOCUMENT_REJECTED)
                .providerEvent("refusal")
                .providerStatus("refused")
                .occurredAt(LocalDateTime.now())
                .build();

        when(envelopeRepository.findByExternalId("env-123")).thenReturn(Optional.of(envelope));
        when(signerRepository.findByExternalId("signer-456")).thenReturn(Optional.of(signer));

        processor.process(event);

        assertEquals(Status.REFUSED, envelope.getStatus());
        assertEquals("REFUSED", signer.getStatus());
        
        verify(envelopeRepository).save(envelope);
        verify(signerRepository).save(signer);
        verify(eventRepository).save(any(EnvelopeEventEntity.class));
        verify(kafkaTemplate).send(eq(KafkaConfig.ENVELOPE_EVENTS_TOPIC), eq(envelope.getExternalId()), eq(event));
    }

    @Test
    void shouldProcessCompletedEvent() {
        envelope.setStatus(Status.ACTIVE);
        
        NormalizedWebhookEvent event = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-123")
                .eventType(WebhookEventType.DOCUMENT_COMPLETED)
                .providerEvent("document_closed")
                .providerStatus("closed")
                .occurredAt(LocalDateTime.now())
                .build();

        when(envelopeRepository.findByExternalId("env-123")).thenReturn(Optional.of(envelope));

        processor.process(event);

        assertEquals(Status.CLOSED, envelope.getStatus());
        
        verify(envelopeRepository).save(envelope);
        verify(eventRepository).save(any(EnvelopeEventEntity.class));
        verify(kafkaTemplate).send(eq(KafkaConfig.ENVELOPE_EVENTS_TOPIC), eq(envelope.getExternalId()), eq(event));
    }
}
