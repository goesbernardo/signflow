package com.signflow.adapter.clicksign;

import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.*;
import com.signflow.exception.domain.IntegrationException;
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

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClickSignGateway")
class ClickSignGatewayTest {

    @Mock
    private ClickSignIntegrationFeignClient clickSignClient;

    @Mock
    private ClickSignMapper mapper;

    @InjectMocks
    private ClickSignGateway gateway;

    // ── Fixtures ──────────────────────────────────────────────────────────

    private SignatureClickSignResponseDTO mockResponse() {
        return SignatureClickSignResponseDTO.builder()
                .data(ClickSignResponseDataDTO.builder()
                        .id("uuid-externo-123")
                        .type("envelopes")
                        .attributes(ClickSignResponseAttributesDTO.builder()
                                .name("Contrato Teste")
                                .status("draft")
                                .created(OffsetDateTime.now())
                                .modified(OffsetDateTime.now())
                                .build())
                        .build())
                .build();
    }

    private Envelope mockEnvelope() {
        return Envelope.builder()
                .externalId("uuid-externo-123")
                .name("Contrato Teste")
                .status(Status.DRAFT)
                .build();
    }

    private Signer mockSigner() {
        return Signer.builder()
                .externalId("signer-uuid-123")
                .name("Bernardo Goes")
                .build();
    }

    private Document mockDocument() {
        return Document.builder()
                .externalId("doc-uuid-123")
                .build();
    }

    private Requirement mockRequirement() {
        return Requirement.builder()
                .externalId("req-uuid-123")
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // provider()
    // ══════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("provider() deve retornar CLICKSIGN")
    void provider_deveRetornarClickSign() {
        assertThat(gateway.provider()).isEqualTo(ProviderSignature.CLICKSIGN);
    }

    // ══════════════════════════════════════════════════════════════════════
    // createEnvelope
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("createEnvelope")
    class CreateEnvelope {

        @Test
        @DisplayName("deve enviar somente o campo name para a ClickSign")
        void deveCriarEnvelopeComSomenteNome() {
            var cmd = CreateEnvelopeCommand.builder().name("Contrato Teste").build();
            var response = mockResponse();
            var envelope = mockEnvelope();

            when(clickSignClient.createEnvelope(any())).thenReturn(response);
            when(mapper.toEnvelopeDomain(response)).thenReturn(envelope);

            var result = gateway.createEnvelope(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getExternalId()).isEqualTo("uuid-externo-123");

            // Verificar body enviado para a ClickSign
            var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
            verify(clickSignClient).createEnvelope(captor.capture());

            var data = (ClickSignRequestApiDataDTO) captor.getValue().data();
            assertThat(data.type()).isEqualTo("envelopes");
            assertThat(data.id()).isNull();
            assertThat(data.relationships()).isNull();

            var attrs = (ClickSignEnvelopeAttributesDTO) data.attributes();
            assertThat(attrs.name()).isEqualTo("Contrato Teste");
        }

        @Test
        @DisplayName("deve repassar IntegrationException original sem duplicar")
        void deveRepassarIntegrationExceptionOriginal() {
            var cmd = CreateEnvelopeCommand.builder().name("Contrato").build();
            var original = new IntegrationException("erro original", null);
            when(clickSignClient.createEnvelope(any())).thenThrow(original);

            assertThatThrownBy(() -> gateway.createEnvelope(cmd))
                    .isSameAs(original);
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
        void deveBuscarEnvelopePeloExternalId() {
            var response = mockResponse();
            var envelope = mockEnvelope();

            when(clickSignClient.getEnvelope("env-id")).thenReturn(response);
            when(mapper.toEnvelopeDomain(response)).thenReturn(envelope);

            var result = gateway.getEnvelope("env-id");

            assertThat(result.getExternalId()).isEqualTo("uuid-externo-123");
        }


        // ══════════════════════════════════════════════════════════════════════
        // addSigner
        // ══════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("addSigner")
        class AddSigner {

            @Test
            @DisplayName("deve mapear NotificationChannel.EMAIL para 'email' na ClickSign")
            void deveMapearEmailCorretamente() {
                var cmd = AddSignerCommand.builder()
                        .name("Bernardo Goes")
                        .email("bernardo@teste.com")
                        .notificationChannel(NotificationChannel.EMAIL)
                        .hasDocumentation(false)
                        .build();

                var response = mockResponse();
                var signer = mockSigner();

                when(clickSignClient.createSigner(eq("env-id"), any())).thenReturn(response);
                when(mapper.toSignerDomain(response)).thenReturn(signer);

                var result = gateway.addSigner("env-id", cmd);

                assertThat(result.getExternalId()).isEqualTo("signer-uuid-123");

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(eq("env-id"), captor.capture());

                var data = (ClickSignRequestApiDataDTO) captor.getValue().data();
                var attrs = (ClickSignCreateSignAttributesDTO) data.attributes();
                var events = attrs.communicateEvents();

                assertThat(events.signatureRequest()).isEqualTo("email");
                assertThat(events.documentSigned()).isEqualTo("email");
            }

            @Test
            @DisplayName("deve mapear NotificationChannel.SMS para 'sms'")
            void deveMapearSmsCorretamente() {
                var cmd = AddSignerCommand.builder()
                        .name("Bernardo")
                        .email("bernardo@teste.com")
                        .phoneNumber("11954381495")
                        .notificationChannel(NotificationChannel.SMS)
                        .hasDocumentation(false)
                        .build();

                when(clickSignClient.createSigner(any(), any())).thenReturn(mockResponse());
                when(mapper.toSignerDomain(any())).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(any(), captor.capture());

                var attrs = (ClickSignCreateSignAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();
                assertThat(attrs.communicateEvents().signatureRequest()).isEqualTo("sms");
            }

            @Test
            @DisplayName("deve mapear NotificationChannel.WHATSAPP para 'whatsapp'")
            void deveMapearWhatsappCorretamente() {
                var cmd = AddSignerCommand.builder()
                        .name("Bernardo")
                        .email("bernardo@teste.com")
                        .phoneNumber("11954381495")
                        .notificationChannel(NotificationChannel.WHATSAPP)
                        .hasDocumentation(false)
                        .build();

                when(clickSignClient.createSigner(any(), any())).thenReturn(mockResponse());
                when(mapper.toSignerDomain(any())).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(any(), captor.capture());

                var attrs = (ClickSignCreateSignAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();
                assertThat(attrs.communicateEvents().signatureRequest()).isEqualTo("whatsapp");
            }

            @Test
            @DisplayName("deve usar 'email' como default quando NotificationChannel for null")
            void deveUsarEmailComoDefaultQuandoChannelForNull() {
                var cmd = AddSignerCommand.builder()
                        .name("Bernardo")
                        .email("bernardo@teste.com")
                        .notificationChannel(null)
                        .hasDocumentation(false)
                        .build();

                when(clickSignClient.createSigner(any(), any())).thenReturn(mockResponse());
                when(mapper.toSignerDomain(any())).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(any(), captor.capture());

                var attrs = (ClickSignCreateSignAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();
                assertThat(attrs.communicateEvents().signatureRequest()).isEqualTo("email");
            }

            @Test
            @DisplayName("deve fazer trim no nome do signatário")
            void deveFazerTrimNoNome() {
                var cmd = AddSignerCommand.builder()
                        .name("  Bernardo Goes  ")
                        .email("bernardo@teste.com")
                        .notificationChannel(NotificationChannel.EMAIL)
                        .hasDocumentation(false)
                        .build();

                when(clickSignClient.createSigner(any(), any())).thenReturn(mockResponse());
                when(mapper.toSignerDomain(any())).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(any(), captor.capture());

                var attrs = (ClickSignCreateSignAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();
                assertThat(attrs.name()).isEqualTo("Bernardo Goes");
            }

            @Test
            @DisplayName("deve definir hasDocumentation automaticamente quando documentation for preenchido")
            void deveDefinirHasDocumentationAutomaticamente() {
                var cmd = AddSignerCommand.builder()
                        .name("Bernardo")
                        .email("bernardo@teste.com")
                        .documentation("916.386.410-05")
                        .hasDocumentation(null)
                        .notificationChannel(NotificationChannel.EMAIL)
                        .build();

                when(clickSignClient.createSigner(any(), any())).thenReturn(mockResponse());
                when(mapper.toSignerDomain(any())).thenReturn(mockSigner());

                gateway.addSigner("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createSigner(any(), captor.capture());

                var attrs = (ClickSignCreateSignAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();
                assertThat(attrs.hasDocumentation()).isTrue();
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // addDocument
        // ══════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("addDocument")
        class AddDocument {

            @Test
            @DisplayName("deve enviar filename e contentBase64 corretamente")
            void deveEnviarDocumentoCorretamente() {
                var cmd = AddDocumentCommand.builder()
                        .filename("contrato.pdf")
                        .contentBase64("JVBERi0xLjQ...")
                        .build();

                when(clickSignClient.createDocument(eq("env-id"), any())).thenReturn(mockResponse());
                when(mapper.toDocumentDomain(any())).thenReturn(mockDocument());

                var result = gateway.addDocument("env-id", cmd);

                assertThat(result.getExternalId()).isEqualTo("doc-uuid-123");

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createDocument(eq("env-id"), captor.capture());

                var data = (ClickSignRequestApiDataDTO) captor.getValue().data();
                assertThat(data.type()).isEqualTo("documents");

                var attrs = (ClickSignCreateDocumentAttributesDTO) data.attributes();
                assertThat(attrs.filename()).isEqualTo("contrato.pdf");
                assertThat(attrs.contentBase64()).isEqualTo("JVBERi0xLjQ...");
            }
        }

        // ══════════════════════════════════════════════════════════════════════
        // addRequirement
        // ══════════════════════════════════════════════════════════════════════

        @Nested
        @DisplayName("addRequirement")
        class AddRequirement {

            @Test
            @DisplayName("deve criar requisito de qualificação quando role for informado")
            void deveCriarRequisitoDe_Qualificacao_QuandoRoleInformado() {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-id")
                        .documentId("doc-id")
                        .role(SignerRole.SIGN)
                        .build();

                when(clickSignClient.createRequirements(eq("env-id"), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                gateway.addRequirement("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createRequirements(eq("env-id"), captor.capture());

                var data = (ClickSignRequestApiDataDTO) captor.getValue().data();
                var attrs = (ClickSignRequirementsAttributesDTO) data.attributes();

                assertThat(attrs.action()).isEqualTo(RequirementAction.AGREE);
                assertThat(attrs.role()).isEqualTo(RequirementRole.SIGN);
                assertThat(attrs.auth()).isNull();
            }

            @Test
            @DisplayName("deve criar requisito de autenticação quando auth for informado")
            void deveCriarRequisito_Autenticacao_QuandoAuthInformado() {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-id")
                        .documentId("doc-id")
                        .auth(SignatureAuthMethod.EMAIL)
                        .build();

                when(clickSignClient.createRequirements(eq("env-id"), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                gateway.addRequirement("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createRequirements(eq("env-id"), captor.capture());

                var attrs = (ClickSignRequirementsAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();

                assertThat(attrs.action()).isEqualTo(RequirementAction.PROVIDE_EVIDENCE);
                assertThat(attrs.auth()).isEqualTo(RequirementAuth.EMAIL);
                assertThat(attrs.role()).isNull();
            }

            @Test
            @DisplayName("deve usar qualificação padrão (agree + sign) quando role e auth forem null")
            void deveUsarQualificacaoPadraoQuandoAmbosNull() {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-id")
                        .documentId("doc-id")
                        .build();

                when(clickSignClient.createRequirements(any(), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                gateway.addRequirement("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createRequirements(any(), captor.capture());

                var attrs = (ClickSignRequirementsAttributesDTO)
                        ((ClickSignRequestApiDataDTO) captor.getValue().data()).attributes();

                assertThat(attrs.action()).isEqualTo(RequirementAction.AGREE);
                assertThat(attrs.role()).isEqualTo(RequirementRole.SIGN);
            }

            @ParameterizedTest
            @EnumSource(SignatureAuthMethod.class)
            @DisplayName("deve mapear todos os SignatureAuthMethod sem lançar exceção")
            void deveMapearTodosAuthMethods(SignatureAuthMethod auth) {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-id")
                        .documentId("doc-id")
                        .auth(auth)
                        .build();

                when(clickSignClient.createRequirements(any(), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                assertThatCode(() -> gateway.addRequirement("env-id", cmd))
                        .doesNotThrowAnyException();
            }

            @ParameterizedTest
            @EnumSource(SignerRole.class)
            @DisplayName("deve mapear todos os SignerRole sem lançar exceção")
            void deveMapearTodosSignerRoles(SignerRole role) {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-id")
                        .documentId("doc-id")
                        .role(role)
                        .build();

                when(clickSignClient.createRequirements(any(), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                assertThatCode(() -> gateway.addRequirement("env-id", cmd))
                        .doesNotThrowAnyException();
            }

            @Test
            @DisplayName("deve incluir relationships com signer e document corretos")
            void deveIncluirRelationshipsCorretamente() {
                var cmd = AddRequirementCommand.builder()
                        .signerId("signer-uuid")
                        .documentId("doc-uuid")
                        .role(SignerRole.SIGN)
                        .build();

                when(clickSignClient.createRequirements(any(), any())).thenReturn(mockResponse());
                when(mapper.toRequirementDomain(any())).thenReturn(mockRequirement());

                gateway.addRequirement("env-id", cmd);

                var captor = ArgumentCaptor.forClass(ClickSignRequestApiDTO.class);
                verify(clickSignClient).createRequirements(any(), captor.capture());

                var data = (ClickSignRequestApiDataDTO) captor.getValue().data();
                var rel = (ClickSignRequirementsRelationshipDTO) data.relationships();

                assertThat(rel.signer().data().id()).isEqualTo("signer-uuid");
                assertThat(rel.signer().data().type()).isEqualTo("signers");
                assertThat(rel.document().data().id()).isEqualTo("doc-uuid");
                assertThat(rel.document().data().type()).isEqualTo("documents");
            }
        }
        // ══════════════════════════════════════════════════════════════════════
        // translateException — tratamento de erros
        // ══════════════════════════════════════════════════════════════════════


        @Test
        @DisplayName("deve repassar IntegrationException sem encapsular novamente")
        void deveRepassarIntegrationExceptionSemEncapsular() {
            var original = new IntegrationException("erro original", null);
            when(clickSignClient.createEnvelope(any())).thenThrow(original);

            var cmd = CreateEnvelopeCommand.builder().name("Teste").build();

            assertThatThrownBy(() -> gateway.createEnvelope(cmd))
                    .isSameAs(original);
        }

    }
}
