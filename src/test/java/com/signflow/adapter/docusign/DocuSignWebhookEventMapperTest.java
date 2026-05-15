package com.signflow.adapter.docusign;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.WebhookEventType;
import com.signflow.infrastructure.provider.docusign.dto.DocuSignEnvelopeResponseDTO;
import com.signflow.infrastructure.provider.docusign.dto.DocuSignWebhookDataDTO;
import com.signflow.infrastructure.provider.docusign.dto.DocuSignWebhookPayloadDTO;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignMapper;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignWebhookEventMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("DocuSignWebhookEventMapper")
class DocuSignWebhookEventMapperTest {

    private DocuSignWebhookEventMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocuSignWebhookEventMapper(new DocuSignMapper());
    }

    // ══════════════════════════════════════════════════════════════════════
    // map(String event)
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("map(String event)")
    class MapEvent {

        @ParameterizedTest
        @CsvSource({
                "envelope-sent,        DOCUMENT_SENT",
                "envelope-delivered,   DOCUMENT_VIEWED",
                "envelope-signed,      DOCUMENT_SIGNED",
                "envelope-completed,   DOCUMENT_COMPLETED",
                "envelope-declined,    DOCUMENT_REJECTED",
                "recipient-declined,   DOCUMENT_REJECTED",
                "envelope-voided,      DOCUMENT_CANCELED",
                "recipient-sent,       DOCUMENT_SENT",
                "recipient-completed,  DOCUMENT_SIGNED",
                "unknown-event,        UNKNOWN"
        })
        @DisplayName("deve mapear evento DocuSign para WebhookEventType")
        void deveMapearEventoCorretamente(String dsEvent, String expectedType) {
            assertThat(mapper.map(dsEvent)).isEqualTo(WebhookEventType.valueOf(expectedType));
        }

        @Test
        @DisplayName("deve retornar UNKNOWN para evento null")
        void deveRetornarUnknownParaNull() {
            assertThat(mapper.map(null)).isEqualTo(WebhookEventType.UNKNOWN);
        }

        @Test
        @DisplayName("deve fazer mapeamento case-insensitive")
        void deveFazerMapeamentoCaseInsensitive() {
            assertThat(mapper.map("ENVELOPE-COMPLETED")).isEqualTo(WebhookEventType.DOCUMENT_COMPLETED);
            assertThat(mapper.map("Envelope-Sent")).isEqualTo(WebhookEventType.DOCUMENT_SENT);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toNormalizedEvent
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toNormalizedEvent")
    class ToNormalizedEvent {

        @Test
        @DisplayName("deve lançar exceção quando payload for null")
        void deveLancarExcecaoParaPayloadNull() {
            assertThatThrownBy(() -> mapper.toNormalizedEvent(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("deve mapear campos básicos do payload corretamente")
        void deveMapearCamposBasicos() {
            var payload = buildPayload("envelope-completed", "env-uuid-123",
                    "acc-456", "completed", "2026-05-15T10:00:00Z");

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event.getEnvelopeExternalId()).isEqualTo("env-uuid-123");
            assertThat(event.getProvider()).isEqualTo(ProviderSignature.DOCUSIGN);
            assertThat(event.getEventType()).isEqualTo(WebhookEventType.DOCUMENT_COMPLETED);
            assertThat(event.getProviderEvent()).isEqualTo("envelope-completed");
            assertThat(event.getProviderStatus()).isEqualTo("completed");
        }

        @Test
        @DisplayName("deve incluir accountId nos metadados")
        void deveIncluirAccountIdNosMetadados() {
            var payload = buildPayload("envelope-sent", "env-id", "account-789",
                    "sent", null);

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event.getMetadata()).containsKey("accountId");
            assertThat(event.getMetadata().get("accountId")).isEqualTo("account-789");
        }

        @Test
        @DisplayName("deve usar LocalDateTime.now() quando generatedDateTime for null")
        void deveUsarNowQuandoGeneratedDateTimeNull() {
            var payload = new DocuSignWebhookPayloadDTO(
                    "envelope-completed", "v2.1", "/uri", "0", "123",
                    null,
                    new DocuSignWebhookDataDTO("acc-id", "env-id", "user-id",
                            new DocuSignEnvelopeResponseDTO("env-id", "completed", "Subject",
                                    null, null, null, null, null)));

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event.getOccurredAt()).isNotNull();
        }

        @Test
        @DisplayName("deve parsear generatedDateTime corretamente")
        void deveParserGeneratedDateTime() {
            var payload = buildPayload("envelope-signed", "env-id", "acc-id",
                    "signed", "2026-03-10T15:45:00Z");

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event.getOccurredAt().getYear()).isEqualTo(2026);
            assertThat(event.getOccurredAt().getMonthValue()).isEqualTo(3);
            assertThat(event.getOccurredAt().getDayOfMonth()).isEqualTo(10);
        }

        @Test
        @DisplayName("deve lidar com data.envelopeSummary null sem exceção")
        void deveLidarComEnvelopeSummaryNull() {
            var payload = new DocuSignWebhookPayloadDTO(
                    "envelope-voided", "v2.1", "/uri", "0", "123",
                    "2026-05-15T10:00:00Z",
                    new DocuSignWebhookDataDTO("acc-id", "env-uuid", "user-id", null));

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event).isNotNull();
            assertThat(event.getProviderStatus()).isNull();
        }

        @Test
        @DisplayName("deve lidar com data null sem exceção")
        void deveLidarComDataNull() {
            var payload = new DocuSignWebhookPayloadDTO(
                    "envelope-completed", "v2.1", "/uri", "0", "123",
                    "2026-05-15T10:00:00Z", null);

            var event = mapper.toNormalizedEvent(payload);

            assertThat(event).isNotNull();
            assertThat(event.getEnvelopeExternalId()).isNull();
        }

        private DocuSignWebhookPayloadDTO buildPayload(String eventName, String envelopeId,
                String accountId, String envelopeStatus, String generatedDateTime) {
            var envelopeSummary = new DocuSignEnvelopeResponseDTO(
                    envelopeId, envelopeStatus, "Subject",
                    "2026-01-01T00:00:00Z", null, null, null, null);
            var data = new DocuSignWebhookDataDTO(accountId, envelopeId, "user-id", envelopeSummary);
            return new DocuSignWebhookPayloadDTO(
                    eventName, "v2.1", "/uri", "0", "123", generatedDateTime, data);
        }
    }
}
