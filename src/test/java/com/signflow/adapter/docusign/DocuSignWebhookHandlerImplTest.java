package com.signflow.adapter.docusign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.webhook.NormalizedWebhookEvent;
import com.signflow.application.webhook.WebhookEventProcessor;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.WebhookEventType;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignWebhookEventMapper;
import com.signflow.infrastructure.provider.docusign.webhook.DocuSignWebhookHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocuSignWebhookHandlerImpl")
class DocuSignWebhookHandlerImplTest {

    @Mock
    private WebhookEventProcessor webhookEventProcessor;

    @Mock
    private DocuSignWebhookEventMapper eventMapper;

    private DocuSignWebhookHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new DocuSignWebhookHandlerImpl(webhookEventProcessor, new ObjectMapper(), eventMapper);
    }

    private static final String VALID_PAYLOAD = """
            {
              "event": "envelope-completed",
              "apiVersion": "v2.1",
              "uri": "/uri",
              "retryCount": "0",
              "configurationId": "123",
              "generatedDateTime": "2026-05-15T10:00:00Z",
              "data": {
                "accountId": "acc-123",
                "envelopeId": "env-456",
                "userId": "user-789",
                "envelopeSummary": {
                  "envelopeId": "env-456",
                  "status": "completed",
                  "emailSubject": "Contrato",
                  "createdDateTime": "2026-05-15T09:00:00Z"
                }
              }
            }
            """;

    @Test
    @DisplayName("deve processar webhook DocuSign com sucesso")
    void deveProcessarWebhookComSucesso() {
        var normalizedEvent = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-456")
                .provider(ProviderSignature.DOCUSIGN)
                .eventType(WebhookEventType.DOCUMENT_COMPLETED)
                .providerEvent("envelope-completed")
                .occurredAt(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(eventMapper.toNormalizedEvent(any())).thenReturn(normalizedEvent);

        handler.handle("docusign", VALID_PAYLOAD);

        verify(webhookEventProcessor).process(normalizedEvent);
    }

    @Test
    @DisplayName("deve aceitar provider em maiúsculas (case-insensitive)")
    void deveAceitarProviderEmMaiusculas() {
        var normalizedEvent = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-456")
                .provider(ProviderSignature.DOCUSIGN)
                .eventType(WebhookEventType.DOCUMENT_COMPLETED)
                .occurredAt(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(eventMapper.toNormalizedEvent(any())).thenReturn(normalizedEvent);

        handler.handle("DOCUSIGN", VALID_PAYLOAD);

        verify(webhookEventProcessor).process(any());
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException para provider desconhecido")
    void deveLancarExcecaoParaProviderDesconhecido() {
        assertThatThrownBy(() -> handler.handle("clicksign", VALID_PAYLOAD))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("clicksign");

        verifyNoInteractions(webhookEventProcessor);
    }

    @Test
    @DisplayName("deve lançar RuntimeException quando payload for JSON inválido")
    void deveLancarExcecaoParaPayloadInvalido() {
        assertThatThrownBy(() -> handler.handle("docusign", "{invalid-json"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("webhook DocuSign");

        verifyNoInteractions(webhookEventProcessor);
    }

    @Test
    @DisplayName("deve desserializar o payload e passar para o mapper")
    void deveDesserializarPayloadEPassarParaMapper() {
        var normalizedEvent = NormalizedWebhookEvent.builder()
                .envelopeExternalId("env-456")
                .provider(ProviderSignature.DOCUSIGN)
                .eventType(WebhookEventType.DOCUMENT_COMPLETED)
                .occurredAt(LocalDateTime.now())
                .metadata(Map.of())
                .build();

        when(eventMapper.toNormalizedEvent(any())).thenReturn(normalizedEvent);

        handler.handle("docusign", VALID_PAYLOAD);

        var captor = ArgumentCaptor.forClass(
                com.signflow.infrastructure.provider.docusign.dto.DocuSignWebhookPayloadDTO.class);
        verify(eventMapper).toNormalizedEvent(captor.capture());

        var payload = captor.getValue();
        assertThat(payload.event()).isEqualTo("envelope-completed");
        assertThat(payload.data().envelopeId()).isEqualTo("env-456");
        assertThat(payload.data().accountId()).isEqualTo("acc-123");
    }
}
