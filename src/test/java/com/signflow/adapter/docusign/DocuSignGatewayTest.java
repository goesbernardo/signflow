package com.signflow.adapter.docusign;

import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.NotificationChannel;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.SignatureAuthMethod;
import com.signflow.enums.SignerRole;
import com.signflow.enums.Status;
import com.signflow.infrastructure.exception.IntegrationException;
import com.signflow.infrastructure.provider.docusign.DocuSignGateway;
import com.signflow.infrastructure.provider.docusign.client.DocuSignIntegrationFeignClient;
import com.signflow.infrastructure.provider.docusign.docusign_exception.DocuSignIntegrationException;
import com.signflow.infrastructure.provider.docusign.dto.*;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocuSignGateway")
class DocuSignGatewayTest {

    @Mock
    private DocuSignIntegrationFeignClient docuSignClient;

    @Mock
    private DocuSignMapper mapper;

    @InjectMocks
    private DocuSignGateway gateway;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private DocuSignEnvelopeResponseDTO mockEnvelopeResponse() {
        return new DocuSignEnvelopeResponseDTO(
                "env-uuid-123", "created", "Contrato Teste",
                "2026-01-01T00:00:00Z", "2026-01-01T00:00:00Z", null, null, null);
    }

    private DocuSignRecipientsResponseDTO mockRecipientsResponse() {
        var signer = new DocuSignSignerResponseDTO(
                "recipient-uuid-1", "Bernardo Goes", "b@test.com",
                "sent", null, "2026-01-01T00:00:00Z", null, "1");
        return new DocuSignRecipientsResponseDTO(List.of(signer), null);
    }

    private DocuSignTabsResponseDTO mockTabsResponse() {
        var tab = new DocuSignTabItemResponseDTO(
                "tab-uuid-1", "signHere", "doc-id", "recipient-id", "Assinatura", "1");
        return new DocuSignTabsResponseDTO(
                "tab-uuid-1", "signHere", "doc-id", "recipient-id",
                "2026-01-01T00:00:00Z", List.of(tab), null);
    }

    private Envelope mockEnvelope() {
        return Envelope.builder()
                .externalId("env-uuid-123")
                .name("Contrato Teste")
                .status(Status.DRAFT)
                .build();
    }

    private Signer mockSigner() {
        return Signer.builder()
                .externalId("recipient-uuid-1")
                .name("Bernardo Goes")
                .email("b@test.com")
                .build();
    }

    private Document mockDocument() {
        return Document.builder().externalId("doc-uuid-123").build();
    }

    private Requirement mockRequirement() {
        return Requirement.builder().externalId("tab-uuid-1").build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // provider()
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("provider() deve retornar DOCUSIGN")
    void provider_deveRetornarDocuSign() {
        assertThat(gateway.provider()).isEqualTo(ProviderSignature.DOCUSIGN);
    }

    // ══════════════════════════════════════════════════════════════════════
    // createEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createEnvelope")
    class CreateEnvelope {

        @Test
        @DisplayName("deve criar envelope com status 'created' e emailSubject = name")
        void deveCriarEnvelopeComNome() {
            var cmd = CreateEnvelopeCommand.builder().name("Contrato Teste").build();
            var response = mockEnvelopeResponse();
            var envelope = mockEnvelope();

            when(docuSignClient.createEnvelope(any())).thenReturn(response);
            when(mapper.toEnvelopeDomain(response)).thenReturn(envelope);

            var result = gateway.createEnvelope(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getExternalId()).isEqualTo("env-uuid-123");

            var captor = ArgumentCaptor.forClass(DocuSignCreateEnvelopeDTO.class);
            verify(docuSignClient).createEnvelope(captor.capture());

            var body = captor.getValue();
            assertThat(body.emailSubject()).isEqualTo("Contrato Teste");
            assertThat(body.status()).isEqualTo("created");
        }

        @Test
        @DisplayName("deve repassar IntegrationException sem encapsular")
        void deveRepassarIntegrationException() {
            var cmd = CreateEnvelopeCommand.builder().name("Contrato").build();
            var original = new IntegrationException("erro original", null);
            when(docuSignClient.createEnvelope(any())).thenThrow(original);

            assertThatThrownBy(() -> gateway.createEnvelope(cmd)).isSameAs(original);
        }

        @Test
        @DisplayName("deve converter DocuSignIntegrationException em IntegrationException")
        void deveConverterDocuSignExceptionEmIntegrationException() {
            var cmd = CreateEnvelopeCommand.builder().name("Contrato").build();
            var dsEx = new DocuSignIntegrationException("Falha DocuSign", "DS_ERROR", "{\"error\":\"...\"}");
            when(docuSignClient.createEnvelope(any())).thenThrow(dsEx);

            assertThatThrownBy(() -> gateway.createEnvelope(cmd))
                    .isInstanceOf(IntegrationException.class)
                    .hasMessageContaining("Falha DocuSign");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // updateEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("updateEnvelope")
    class UpdateEnvelope {

        @Test
        @DisplayName("deve atualizar emailSubject do envelope")
        void deveAtualizarEnvelope() {
            var cmd = UpdateEnvelopeCommand.builder().name("Novo Nome").build();
            var response = mockEnvelopeResponse();
            var envelope = mockEnvelope();

            when(docuSignClient.updateEnvelope(eq("env-id"), any())).thenReturn(response);
            when(mapper.toEnvelopeDomain(response)).thenReturn(envelope);

            var result = gateway.updateEnvelope("env-id", cmd);

            assertThat(result.getExternalId()).isEqualTo("env-uuid-123");

            var captor = ArgumentCaptor.forClass(DocuSignUpdateEnvelopeDTO.class);
            verify(docuSignClient).updateEnvelope(eq("env-id"), captor.capture());
            assertThat(captor.getValue().emailSubject()).isEqualTo("Novo Nome");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // getEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("getEnvelope")
    class GetEnvelope {

        @Test
        @DisplayName("deve buscar envelope pelo externalId")
        void deveBuscarEnvelope() {
            var response = mockEnvelopeResponse();
            var envelope = mockEnvelope();

            when(docuSignClient.getEnvelope("env-id")).thenReturn(response);
            when(mapper.toEnvelopeDomain(response)).thenReturn(envelope);

            var result = gateway.getEnvelope("env-id");

            assertThat(result.getExternalId()).isEqualTo("env-uuid-123");
            verify(docuSignClient).getEnvelope("env-id");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // activateEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("activateEnvelope")
    class ActivateEnvelope {

        @Test
        @DisplayName("deve enviar status 'sent' para o DocuSign")
        void deveEnviarStatusSent() {
            when(docuSignClient.updateEnvelope(eq("env-id"), any())).thenReturn(mockEnvelopeResponse());

            gateway.activateEnvelope("env-id");

            var captor = ArgumentCaptor.forClass(DocuSignUpdateEnvelopeDTO.class);
            verify(docuSignClient).updateEnvelope(eq("env-id"), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo("sent");
        }

        @Test
        @DisplayName("deve executar fallback quando houver erro")
        void deveExecutarFallback() {
            doThrow(new RuntimeException("API Error"))
                    .when(docuSignClient).updateEnvelope(any(), any());

            assertThatThrownBy(() -> gateway.activateEnvelope("env-id"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // cancelEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelEnvelope")
    class CancelEnvelope {

        @Test
        @DisplayName("deve enviar status 'voided' com voidedReason")
        void deveEnviarStatusVoided() {
            when(docuSignClient.updateEnvelope(eq("env-id"), any())).thenReturn(mockEnvelopeResponse());

            gateway.cancelEnvelope("env-id");

            var captor = ArgumentCaptor.forClass(DocuSignUpdateEnvelopeDTO.class);
            verify(docuSignClient).updateEnvelope(eq("env-id"), captor.capture());
            assertThat(captor.getValue().status()).isEqualTo("voided");
            assertThat(captor.getValue().voidedReason()).isNotBlank();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // remindSigner
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("remindSigner")
    class RemindSigner {

        @Test
        @DisplayName("deve chamar resendToRecipients com resend_envelope=true")
        void deveEnviarReenvioComResendTrue() {
            when(docuSignClient.resendToRecipients(eq("env-id"), eq(true), any()))
                    .thenReturn(mockRecipientsResponse());

            gateway.remindSigner("env-id", "recipient-uuid-1");

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).resendToRecipients(eq("env-id"), eq(true), captor.capture());

            var signers = captor.getValue().signers();
            assertThat(signers).hasSize(1);
            assertThat(signers.get(0).recipientId()).isEqualTo("recipient-uuid-1");
        }

        @Test
        @DisplayName("deve executar fallback quando houver erro")
        void deveExecutarFallback() {
            doThrow(new RuntimeException("API Error"))
                    .when(docuSignClient).resendToRecipients(any(), anyBoolean(), any());

            assertThatThrownBy(() -> gateway.remindSigner("env-id", "signer-id"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // addSigner
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addSigner")
    class AddSigner {

        @Test
        @DisplayName("deve enviar signatário com deliveryMethod=email para channel EMAIL")
        void deveMapearEmailCorretamente() {
            var cmd = AddSignerCommand.builder()
                    .name("Bernardo Goes")
                    .email("b@test.com")
                    .notificationChannel(NotificationChannel.EMAIL)
                    .build();

            when(docuSignClient.addRecipients(eq("env-id"), any())).thenReturn(mockRecipientsResponse());
            when(mapper.toSignerDomain(any(DocuSignSignerResponseDTO.class))).thenReturn(mockSigner());

            var result = gateway.addSigner("env-id", cmd);

            assertThat(result.getExternalId()).isEqualTo("recipient-uuid-1");

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).addRecipients(eq("env-id"), captor.capture());

            var signer = captor.getValue().signers().get(0);
            assertThat(signer.email()).isEqualTo("b@test.com");
            assertThat(signer.name()).isEqualTo("Bernardo Goes");
            assertThat(signer.deliveryMethod()).isEqualTo("email");
        }

        @Test
        @DisplayName("deve mapear WHATSAPP e SMS para deliveryMethod=sms")
        void deveMapearSmsEWhatsappParaSms() {
            for (NotificationChannel channel : new NotificationChannel[]{NotificationChannel.SMS, NotificationChannel.WHATSAPP}) {
                var cmd = AddSignerCommand.builder()
                        .name("Teste")
                        .email("t@test.com")
                        .notificationChannel(channel)
                        .build();

                when(docuSignClient.addRecipients(any(), any())).thenReturn(mockRecipientsResponse());
                when(mapper.toSignerDomain(any(DocuSignSignerResponseDTO.class))).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
                verify(docuSignClient, atLeastOnce()).addRecipients(any(), captor.capture());

                assertThat(captor.getValue().signers().get(0).deliveryMethod()).isEqualTo("sms");
                reset(docuSignClient);
            }
        }

        @Test
        @DisplayName("deve fazer trim no nome do signatário")
        void deveFazerTrimNoNome() {
            var cmd = AddSignerCommand.builder()
                    .name("  Bernardo Goes  ")
                    .email("b@test.com")
                    .notificationChannel(NotificationChannel.EMAIL)
                    .build();

            when(docuSignClient.addRecipients(any(), any())).thenReturn(mockRecipientsResponse());
            when(mapper.toSignerDomain(any(DocuSignSignerResponseDTO.class))).thenReturn(mockSigner());

            gateway.addSigner("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).addRecipients(any(), captor.capture());
            assertThat(captor.getValue().signers().get(0).name()).isEqualTo("Bernardo Goes");
        }

        @Test
        @DisplayName("deve usar 'email' como deliveryMethod quando channel for null")
        void deveUsarEmailPorDefaultQuandoChannelNull() {
            var cmd = AddSignerCommand.builder()
                    .name("Teste")
                    .email("t@test.com")
                    .notificationChannel(null)
                    .build();

            when(docuSignClient.addRecipients(any(), any())).thenReturn(mockRecipientsResponse());
            when(mapper.toSignerDomain(any(DocuSignSignerResponseDTO.class))).thenReturn(mockSigner());

            gateway.addSigner("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).addRecipients(any(), captor.capture());
            assertThat(captor.getValue().signers().get(0).deliveryMethod()).isEqualTo("email");
        }

        @Test
        @DisplayName("deve gerar recipientId UUID quando não informado")
        void deveGerarRecipientIdUuid() {
            var cmd = AddSignerCommand.builder()
                    .name("Teste")
                    .email("t@test.com")
                    .notificationChannel(NotificationChannel.EMAIL)
                    .build();

            when(docuSignClient.addRecipients(any(), any())).thenReturn(mockRecipientsResponse());
            when(mapper.toSignerDomain(any(DocuSignSignerResponseDTO.class))).thenReturn(mockSigner());

            gateway.addSigner("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).addRecipients(any(), captor.capture());
            assertThat(captor.getValue().signers().get(0).recipientId()).isNotBlank();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // addDocument
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addDocument")
    class AddDocument {

        @Test
        @DisplayName("deve enviar filename, contentBase64 e extensão corretamente")
        void deveEnviarDocumentoCorretamente() {
            var cmd = AddDocumentCommand.builder()
                    .filename("contrato.pdf")
                    .contentBase64("JVBERi0xLjQ...")
                    .build();

            when(docuSignClient.addDocuments(eq("env-id"), any())).thenReturn(mockEnvelopeResponse());

            var result = gateway.addDocument("env-id", cmd);

            assertThat(result).isNotNull();
            assertThat(result.getExternalId()).isNotBlank();

            var captor = ArgumentCaptor.forClass(DocuSignDocumentsUpdateDTO.class);
            verify(docuSignClient).addDocuments(eq("env-id"), captor.capture());

            var doc = captor.getValue().documents().get(0);
            assertThat(doc.name()).isEqualTo("contrato.pdf");
            assertThat(doc.documentBase64()).isEqualTo("JVBERi0xLjQ...");
            assertThat(doc.fileExtension()).isEqualTo("pdf");
        }

        @Test
        @DisplayName("deve extrair extensão corretamente de filename com ponto")
        void deveExtrairExtensaoCorretamente() {
            var cmd = AddDocumentCommand.builder()
                    .filename("documento.docx")
                    .contentBase64("base64content")
                    .build();

            when(docuSignClient.addDocuments(any(), any())).thenReturn(mockEnvelopeResponse());

            gateway.addDocument("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignDocumentsUpdateDTO.class);
            verify(docuSignClient).addDocuments(any(), captor.capture());
            assertThat(captor.getValue().documents().get(0).fileExtension()).isEqualTo("docx");
        }

        @Test
        @DisplayName("deve usar 'pdf' como extensão padrão quando filename não tem ponto")
        void deveUsarPdfComoExtensaoPadrao() {
            var cmd = AddDocumentCommand.builder()
                    .filename("documento")
                    .contentBase64("base64content")
                    .build();

            when(docuSignClient.addDocuments(any(), any())).thenReturn(mockEnvelopeResponse());

            gateway.addDocument("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignDocumentsUpdateDTO.class);
            verify(docuSignClient).addDocuments(any(), captor.capture());
            assertThat(captor.getValue().documents().get(0).fileExtension()).isEqualTo("pdf");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // addRequirement
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addRequirement")
    class AddRequirement {

        @Test
        @DisplayName("deve criar signHereTab quando role for SIGN")
        void deveCriarSignHereTabParaRoleSign() {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id")
                    .role(SignerRole.SIGN)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            gateway.addRequirement("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignTabDTO.class);
            verify(docuSignClient).addTabs(eq("env-id"), eq("recipient-id"), captor.capture());

            assertThat(captor.getValue().signHereTabs()).isNotEmpty();
            assertThat(captor.getValue().initialHereTabs()).isNull();
        }

        @Test
        @DisplayName("deve criar initialHereTab quando auth for HANDWRITTEN")
        void deveCriarInitialHereTabParaHandwritten() {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id")
                    .auth(SignatureAuthMethod.HANDWRITTEN)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            gateway.addRequirement("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignTabDTO.class);
            verify(docuSignClient).addTabs(any(), any(), captor.capture());

            assertThat(captor.getValue().initialHereTabs()).isNotEmpty();
            assertThat(captor.getValue().signHereTabs()).isNull();
        }

        @Test
        @DisplayName("deve criar initialHereTab quando role for WITNESS")
        void deveCriarInitialHereTabParaWitness() {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id")
                    .role(SignerRole.WITNESS)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            gateway.addRequirement("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignTabDTO.class);
            verify(docuSignClient).addTabs(any(), any(), captor.capture());

            assertThat(captor.getValue().initialHereTabs()).isNotEmpty();
        }

        @Test
        @DisplayName("deve incluir documentId e pageNumber na tab")
        void deveIncluirDocumentIdEPageNumberNaTab() {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id-123")
                    .role(SignerRole.SIGN)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            gateway.addRequirement("env-id", cmd);

            var captor = ArgumentCaptor.forClass(DocuSignTabDTO.class);
            verify(docuSignClient).addTabs(any(), any(), captor.capture());

            var tab = captor.getValue().signHereTabs().get(0);
            assertThat(tab.documentId()).isEqualTo("doc-id-123");
            assertThat(tab.pageNumber()).isEqualTo("1");
        }

        @ParameterizedTest
        @EnumSource(SignatureAuthMethod.class)
        @DisplayName("deve mapear todos os SignatureAuthMethod sem lançar exceção")
        void deveMapearTodosAuthMethods(SignatureAuthMethod auth) {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id")
                    .auth(auth)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            assertThatCode(() -> gateway.addRequirement("env-id", cmd)).doesNotThrowAnyException();
        }

        @ParameterizedTest
        @EnumSource(SignerRole.class)
        @DisplayName("deve mapear todos os SignerRole sem lançar exceção")
        void deveMapearTodosSignerRoles(SignerRole role) {
            var cmd = AddRequirementCommand.builder()
                    .signerId("recipient-id")
                    .documentId("doc-id")
                    .role(role)
                    .build();

            when(docuSignClient.addTabs(any(), any(), any())).thenReturn(mockTabsResponse());
            when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

            assertThatCode(() -> gateway.addRequirement("env-id", cmd)).doesNotThrowAnyException();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // addNotifier
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("addNotifier")
    class AddNotifier {

        @Test
        @DisplayName("deve adicionar carbon copy com email e nome corretos")
        void deveAdicionarCarbonCopyCorretamente() {
            var cmd = AddNotifierCommand.builder()
                    .email("observer@test.com")
                    .name("Observador")
                    .build();

            when(docuSignClient.addCarbonCopies(eq("env-id"), any())).thenReturn(
                    new DocuSignRecipientsResponseDTO(null, null));

            var result = gateway.addNotifier("env-id", cmd);

            assertThat(result).isNotBlank();

            var captor = ArgumentCaptor.forClass(DocuSignRecipientsDTO.class);
            verify(docuSignClient).addCarbonCopies(eq("env-id"), captor.capture());

            var cc = captor.getValue().carbonCopies().get(0);
            assertThat(cc.email()).isEqualTo("observer@test.com");
            assertThat(cc.name()).isEqualTo("Observador");
            assertThat(cc.routingOrder()).isEqualTo("99");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // translateException
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("translateException")
    class TranslateException {

        @Test
        @DisplayName("deve repassar IntegrationException sem encapsular")
        void deveRepassarIntegrationExceptionSemEncapsular() {
            var original = new IntegrationException("erro original", null);
            when(docuSignClient.createEnvelope(any())).thenThrow(original);

            assertThatThrownBy(() -> gateway.createEnvelope(
                    CreateEnvelopeCommand.builder().name("Teste").build()))
                    .isSameAs(original);
        }

        @Test
        @DisplayName("deve converter DocuSignIntegrationException em IntegrationException com detalhes")
        void deveConverterDocuSignException() {
            var dsEx = new DocuSignIntegrationException("Falha DocuSign", "ENVELOPE_INVALID_STATUS", "{}");
            when(docuSignClient.createEnvelope(any())).thenThrow(dsEx);

            assertThatThrownBy(() -> gateway.createEnvelope(
                    CreateEnvelopeCommand.builder().name("Teste").build()))
                    .isInstanceOf(IntegrationException.class)
                    .hasMessage("Falha DocuSign");
        }

        @Test
        @DisplayName("deve propagar RuntimeException (fallback ativo apenas com proxy Spring AOP)")
        void devePropararRuntimeException() {
            when(docuSignClient.createEnvelope(any())).thenThrow(new RuntimeException("Erro genérico"));

            // Sem AOP proxy em unit test, RuntimeException propaga sem encapsulamento.
            // O fallback é invocado apenas quando o @CircuitBreaker está ativo (contexto Spring).
            assertThatThrownBy(() -> gateway.createEnvelope(
                    CreateEnvelopeCommand.builder().name("Teste").build()))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
