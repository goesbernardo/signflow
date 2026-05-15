package com.signflow.adapter.docusign;

import com.signflow.enums.Status;
import com.signflow.infrastructure.provider.docusign.dto.*;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DocuSignMapper")
class DocuSignMapperTest {

    private DocuSignMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new DocuSignMapper();
    }

    // ══════════════════════════════════════════════════════════════════════
    // toEnvelopeDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toEnvelopeDomain")
    class ToEnvelopeDomain {

        @Test
        @DisplayName("deve retornar null quando response for null")
        void deveRetornarNullParaResponseNull() {
            assertThat(mapper.toEnvelopeDomain(null)).isNull();
        }

        @ParameterizedTest
        @CsvSource({
                "created,   DRAFT",
                "sent,      ACTIVE",
                "delivered, ACTIVE",
                "signed,    PENDING",
                "completed, CLOSED",
                "declined,  REFUSED",
                "voided,    CANCELED",
                "deleted,   DELETED"
        })
        @DisplayName("deve mapear status DocuSign para Status do domínio corretamente")
        void deveMapearStatusCorretamente(String dsStatus, String expectedStatus) {
            var response = new DocuSignEnvelopeResponseDTO(
                    "env-uuid", dsStatus, "Contrato",
                    "2026-01-01T00:00:00Z", null, null, null, null);

            var envelope = mapper.toEnvelopeDomain(response);

            assertThat(envelope).isNotNull();
            assertThat(envelope.getStatus()).isEqualTo(Status.valueOf(expectedStatus));
        }

        @Test
        @DisplayName("deve mapear status desconhecido para PENDING com log de warning")
        void deveMapearStatusDesconhecidoParaPending() {
            var response = new DocuSignEnvelopeResponseDTO(
                    "env-uuid", "unknown_status", "Contrato",
                    null, null, null, null, null);

            var envelope = mapper.toEnvelopeDomain(response);

            assertThat(envelope.getStatus()).isEqualTo(Status.PENDING);
        }

        @Test
        @DisplayName("deve mapear envelopeId para externalId")
        void deveMapearEnvelopeIdParaExternalId() {
            var response = new DocuSignEnvelopeResponseDTO(
                    "env-uuid-abc", "created", "Meu Contrato",
                    "2026-01-01T00:00:00Z", null, null, null, null);

            var envelope = mapper.toEnvelopeDomain(response);

            assertThat(envelope.getExternalId()).isEqualTo("env-uuid-abc");
            assertThat(envelope.getName()).isEqualTo("Meu Contrato");
            assertThat(envelope.getProvider()).isEqualTo("DOCUSIGN");
        }

        @Test
        @DisplayName("deve mapear createdDateTime corretamente")
        void deveMapearCreatedDateTime() {
            var response = new DocuSignEnvelopeResponseDTO(
                    "env-uuid", "created", "Contrato",
                    "2026-05-15T10:30:00Z", null, null, null, null);

            var envelope = mapper.toEnvelopeDomain(response);

            assertThat(envelope.getCreated()).isNotNull();
            assertThat(envelope.getCreated().getYear()).isEqualTo(2026);
            assertThat(envelope.getCreated().getMonthValue()).isEqualTo(5);
        }

        @Test
        @DisplayName("deve lidar com status null sem exceção")
        void deveLidarComStatusNull() {
            var response = new DocuSignEnvelopeResponseDTO(
                    "env-uuid", null, "Contrato",
                    null, null, null, null, null);

            var envelope = mapper.toEnvelopeDomain(response);

            assertThat(envelope).isNotNull();
            assertThat(envelope.getStatus()).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toSignerDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toSignerDomain")
    class ToSignerDomain {

        @Test
        @DisplayName("deve retornar null quando dto for null")
        void deveRetornarNullParaDtoNull() {
            assertThat(mapper.toSignerDomain(null)).isNull();
        }

        @Test
        @DisplayName("deve mapear recipientId para externalId")
        void deveMapearRecipientIdParaExternalId() {
            var dto = new DocuSignSignerResponseDTO(
                    "recipient-uuid-1", "Bernardo Goes", "b@test.com",
                    "sent", "2026-05-15T10:00:00Z", "2026-05-15T09:00:00Z",
                    "2026-05-15T09:30:00Z", "1");

            var signer = mapper.toSignerDomain(dto);

            assertThat(signer.getExternalId()).isEqualTo("recipient-uuid-1");
            assertThat(signer.getName()).isEqualTo("Bernardo Goes");
            assertThat(signer.getEmail()).isEqualTo("b@test.com");
            assertThat(signer.getStatus()).isEqualTo("sent");
            assertThat(signer.getSignedAt()).isNotNull();
        }

        @Test
        @DisplayName("deve lidar com datas null sem exceção")
        void deveLidarComDatasNull() {
            var dto = new DocuSignSignerResponseDTO(
                    "recipient-uuid-1", "Teste", "t@test.com",
                    "created", null, null, null, "1");

            var signer = mapper.toSignerDomain(dto);

            assertThat(signer).isNotNull();
            assertThat(signer.getSignedAt()).isNull();
            assertThat(signer.getCreated()).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toSignerListDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toSignerListDomain")
    class ToSignerListDomain {

        @Test
        @DisplayName("deve retornar lista vazia quando response for null")
        void deveRetornarListaVaziaParaNull() {
            assertThat(mapper.toSignerListDomain(null)).isEmpty();
        }

        @Test
        @DisplayName("deve retornar lista vazia quando signers for null")
        void deveRetornarListaVaziaQuandoSignersNull() {
            var response = new DocuSignRecipientsResponseDTO(null, null);
            assertThat(mapper.toSignerListDomain(response)).isEmpty();
        }

        @Test
        @DisplayName("deve mapear lista de signatários corretamente")
        void deveMapearListaDeSignatarios() {
            var s1 = new DocuSignSignerResponseDTO("r-1", "Alice", "alice@test.com", "sent", null, null, null, "1");
            var s2 = new DocuSignSignerResponseDTO("r-2", "Bob", "bob@test.com", "completed", null, null, null, "2");
            var response = new DocuSignRecipientsResponseDTO(List.of(s1, s2), null);

            var signers = mapper.toSignerListDomain(response);

            assertThat(signers).hasSize(2);
            assertThat(signers.get(0).getExternalId()).isEqualTo("r-1");
            assertThat(signers.get(1).getExternalId()).isEqualTo("r-2");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toDocumentDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toDocumentDomain")
    class ToDocumentDomain {

        @Test
        @DisplayName("deve retornar null quando dto for null")
        void deveRetornarNullParaDtoNull() {
            assertThat(mapper.toDocumentDomain(null)).isNull();
        }

        @Test
        @DisplayName("deve mapear documentId para externalId")
        void deveMapearDocumentIdParaExternalId() {
            var dto = new DocuSignDocumentResponseDTO("doc-uuid-1", "contrato.pdf", "content", "1", "/uri");

            var doc = mapper.toDocumentDomain(dto);

            assertThat(doc.getExternalId()).isEqualTo("doc-uuid-1");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toDocumentListDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toDocumentListDomain")
    class ToDocumentListDomain {

        @Test
        @DisplayName("deve retornar lista vazia quando response for null")
        void deveRetornarListaVaziaParaNull() {
            assertThat(mapper.toDocumentListDomain(null)).isEmpty();
        }

        @Test
        @DisplayName("deve mapear lista de documentos corretamente")
        void deveMapearListaDeDocumentos() {
            var d1 = new DocuSignDocumentResponseDTO("d-1", "doc1.pdf", "content", "1", "/uri1");
            var d2 = new DocuSignDocumentResponseDTO("d-2", "doc2.pdf", "content", "2", "/uri2");
            var response = new DocuSignDocumentsListDTO(List.of(d1, d2));

            var docs = mapper.toDocumentListDomain(response);

            assertThat(docs).hasSize(2);
            assertThat(docs.get(0).getExternalId()).isEqualTo("d-1");
            assertThat(docs.get(1).getExternalId()).isEqualTo("d-2");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // toRequirementDomain
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("toRequirementDomain")
    class ToRequirementDomain {

        @Test
        @DisplayName("deve retornar null quando response for null")
        void deveRetornarNullParaNull() {
            assertThat(mapper.toRequirementDomain(null)).isNull();
        }

        @Test
        @DisplayName("deve usar tabId do campo raiz quando disponível")
        void deveUsarTabIdDoRaiz() {
            var tab = new DocuSignTabItemResponseDTO("tab-uuid", "signHere", "doc", "rec", "Assinatura", "1");
            var response = new DocuSignTabsResponseDTO(
                    "tab-uuid", "signHere", "doc", "rec", null, List.of(tab), null);

            var req = mapper.toRequirementDomain(response);

            assertThat(req.getExternalId()).isEqualTo("tab-uuid");
        }

        @Test
        @DisplayName("deve extrair tabId de signHereTabs quando tabId do raiz for null")
        void deveExtrairTabIdDeSignHereTabs() {
            var tab = new DocuSignTabItemResponseDTO("inner-tab-uuid", "signHere", "doc", "rec", "Assinatura", "1");
            var response = new DocuSignTabsResponseDTO(
                    null, "signHere", "doc", "rec", null, List.of(tab), null);

            var req = mapper.toRequirementDomain(response);

            assertThat(req.getExternalId()).isEqualTo("inner-tab-uuid");
        }
    }
}
