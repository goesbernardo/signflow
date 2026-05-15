package com.signflow.infrastructure.provider.docusign;

import com.signflow.application.port.out.ESignatureGateway;
import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.NotificationChannel;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.SignatureAuthMethod;
import com.signflow.enums.SignerRole;
import com.signflow.infrastructure.exception.ErroDetail;
import com.signflow.infrastructure.exception.IntegrationException;
import com.signflow.infrastructure.provider.docusign.client.DocuSignIntegrationFeignClient;
import com.signflow.infrastructure.provider.docusign.docusign_exception.DocuSignIntegrationException;
import com.signflow.infrastructure.provider.docusign.dto.*;
import com.signflow.infrastructure.provider.docusign.mapper.DocuSignMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "signflow.providers.docusign", name = "enabled", havingValue = "true")
public class DocuSignGateway implements ESignatureGateway {

    private final DocuSignIntegrationFeignClient docuSignClient;
    private final DocuSignMapper mapper;

    private static final String ERR_UNAVAILABLE  = "Provedor DocuSign indisponível";
    private static final String ERR_FETCH_FAILED = "Não foi possível consultar o DocuSign";

    // ── createEnvelope ────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public Envelope createEnvelope(CreateEnvelopeCommand cmd) {
        var request = DocuSignCreateEnvelopeDTO.builder()
                .emailSubject(cmd.name())
                .status("created")
                .build();
        log.info("Criando envelope no DocuSign: {}", cmd.name());
        var response = docuSignClient.createEnvelope(request);
        log.info("Envelope criado no DocuSign: {}", response.envelopeId());
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope createEnvelopeFallback(CreateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback createEnvelope — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── updateEnvelope ────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "updateEnvelopeFallback")
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd) {
        var request = DocuSignUpdateEnvelopeDTO.builder()
                .emailSubject(cmd.name())
                .build();
        log.info("Atualizando envelope {} no DocuSign", externalId);
        var response = docuSignClient.updateEnvelope(externalId, request);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope updateEnvelopeFallback(String externalId, UpdateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback updateEnvelope — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── getEnvelope ───────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "getEnvelopeFallback")
    public Envelope getEnvelope(String externalId) {
        return mapper.toEnvelopeDomain(docuSignClient.getEnvelope(externalId));
    }

    private Envelope getEnvelopeFallback(String externalId, Throwable t) {
        log.error("Fallback getEnvelope — DocuSign: {}", t.getMessage());
        throw translateException(t, ERR_FETCH_FAILED);
    }

    // ── activateEnvelope ──────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "activateEnvelopeFallback")
    public void activateEnvelope(String envelopeId) {
        var request = DocuSignUpdateEnvelopeDTO.builder()
                .status("sent")
                .build();
        log.info("Ativando envelope {} no DocuSign", envelopeId);
        docuSignClient.updateEnvelope(envelopeId, request);
    }

    private void activateEnvelopeFallback(String envelopeId, Throwable t) {
        log.error("Fallback activateEnvelope — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── cancelEnvelope ────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "cancelEnvelopeFallback")
    public void cancelEnvelope(String envelopeId) {
        var request = DocuSignUpdateEnvelopeDTO.builder()
                .status("voided")
                .voidedReason("Cancelado pelo sistema SignFlow")
                .build();
        log.info("Cancelando envelope {} no DocuSign", envelopeId);
        docuSignClient.updateEnvelope(envelopeId, request);
    }

    private void cancelEnvelopeFallback(String envelopeId, Throwable t) {
        log.error("Fallback cancelEnvelope — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── remindSigner ──────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "remindSignerFallback")
    public void remindSigner(String envelopeId, String recipientId) {
        log.info("Reenviando lembrete para signatário {} no envelope {} (DocuSign)", recipientId, envelopeId);
        var recipients = DocuSignRecipientsDTO.builder()
                .signers(List.of(DocuSignSignerDTO.builder()
                        .recipientId(recipientId)
                        .build()))
                .build();
        docuSignClient.resendToRecipients(envelopeId, true, recipients);
    }

    private void remindSignerFallback(String envelopeId, String recipientId, Throwable t) {
        log.error("Fallback remindSigner — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addSigner ─────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "addSignerFallback")
    public Signer addSigner(String envelopeId, AddSignerCommand cmd) {
        var signer = DocuSignSignerDTO.builder()
                .name(cmd.name() != null ? cmd.name().trim() : null)
                .email(cmd.email())
                .recipientId(java.util.UUID.randomUUID().toString())
                .routingOrder("1")
                .deliveryMethod(mapDeliveryMethod(cmd.notificationChannel()))
                .build();

        var request = DocuSignRecipientsDTO.builder()
                .signers(List.of(signer))
                .build();

        log.info("Adicionando signatário ao envelope {} no DocuSign", envelopeId);
        var response = docuSignClient.addRecipients(envelopeId, request);

        if (response.signers() != null && !response.signers().isEmpty()) {
            return mapper.toSignerDomain(response.signers().get(0));
        }
        return Signer.builder().externalId(signer.recipientId()).name(cmd.name()).email(cmd.email()).build();
    }

    private Signer addSignerFallback(String envelopeId, AddSignerCommand cmd, Throwable t) {
        log.error("Fallback addSigner — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addDocument ───────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "addDocumentFallback")
    public Document addDocument(String envelopeId, AddDocumentCommand cmd) {
        String extension = extractExtension(cmd.filename());
        var doc = DocuSignDocumentDTO.builder()
                .name(cmd.filename())
                .documentId(java.util.UUID.randomUUID().toString())
                .fileExtension(extension)
                .documentBase64(cmd.contentBase64())
                .build();

        var request = DocuSignDocumentsUpdateDTO.builder()
                .documents(List.of(doc))
                .build();

        log.info("Adicionando documento {} ao envelope {} no DocuSign", cmd.filename(), envelopeId);
        docuSignClient.addDocuments(envelopeId, request);

        return Document.builder().externalId(doc.documentId()).build();
    }

    private Document addDocumentFallback(String envelopeId, AddDocumentCommand cmd, Throwable t) {
        log.error("Fallback addDocument — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addRequirement ────────────────────────────────────────────────────

    /**
     * No DocuSign, requisitos de assinatura são representados como "Tabs" —
     * campos posicionados em documentos para cada destinatário.
     * <p>
     * Mapeamento:
     * - SIGN / EMAIL / SMS / WHATSAPP / PIX / API / AUTO → signHereTab
     * - HANDWRITTEN → initialHereTab (rubrica)
     * - FACIAL_BIOMETRICS → signHereTab (biometria configurada via Identity Verification no console DocuSign)
     */
    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "addRequirementFallback")
    public Requirement addRequirement(String envelopeId, AddRequirementCommand cmd) {
        var tab = buildTab(cmd);
        log.info("Adicionando tab (requisito) ao envelope {} / signatário {} no DocuSign",
                envelopeId, cmd.signerId());

        var response = docuSignClient.addTabs(envelopeId, cmd.signerId(), tab);
        return mapper.toRequirementDomain(response);
    }

    private Requirement addRequirementFallback(String envelopeId, AddRequirementCommand cmd, Throwable t) {
        log.error("Fallback addRequirement — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addNotifier ───────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "docusign-circuit-breaker", fallbackMethod = "addNotifierFallback")
    public String addNotifier(String envelopeId, AddNotifierCommand cmd) {
        log.info("Adicionando observador {} ao envelope {} no DocuSign", cmd.email(), envelopeId);
        var carbonCopy = DocuSignCarbonCopyDTO.builder()
                .email(cmd.email())
                .name(cmd.name())
                .recipientId(java.util.UUID.randomUUID().toString())
                .routingOrder("99")
                .build();

        var request = DocuSignRecipientsDTO.builder()
                .carbonCopies(List.of(carbonCopy))
                .build();

        var response = docuSignClient.addCarbonCopies(envelopeId, request);
        return carbonCopy.recipientId();
    }

    private void addNotifierFallback(String envelopeId, AddNotifierCommand cmd, Throwable t) {
        log.error("Fallback addNotifier — DocuSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── provider ──────────────────────────────────────────────────────────

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.DOCUSIGN;
    }

    // ── Mapeamentos internos ──────────────────────────────────────────────

    private String mapDeliveryMethod(NotificationChannel channel) {
        if (channel == null) return "email";
        return switch (channel) {
            case EMAIL            -> "email";
            case SMS, WHATSAPP   -> "sms";
        };
    }

    private DocuSignTabDTO buildTab(AddRequirementCommand cmd) {
        var tabItem = DocuSignSignHereTabDTO.builder()
                .documentId(cmd.documentId())
                .pageNumber("1")
                .tabLabel("Assinatura")
                .build();

        boolean isInitials = cmd.auth() == SignatureAuthMethod.HANDWRITTEN
                || (cmd.role() == SignerRole.WITNESS);

        if (isInitials) {
            return DocuSignTabDTO.builder()
                    .initialHereTabs(List.of(tabItem))
                    .build();
        }
        return DocuSignTabDTO.builder()
                .signHereTabs(List.of(tabItem))
                .build();
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "pdf";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    // ── Tratamento de erros ───────────────────────────────────────────────

    private RuntimeException translateException(Throwable t) {
        return translateException(t, ERR_UNAVAILABLE);
    }

    private RuntimeException translateException(Throwable t, String defaultMessage) {
        if (t instanceof DocuSignIntegrationException dsEx) {
            String message = dsEx.getMessage() != null && !dsEx.getMessage().isBlank()
                    ? dsEx.getMessage()
                    : defaultMessage;
            List<ErroDetail> details = dsEx.getDsErrorCode() != null
                    ? List.of(ErroDetail.builder()
                        .code(dsEx.getDsErrorCode())
                        .message(message)
                        .build())
                    : null;
            return new IntegrationException(message, dsEx.getRawResponse(), details, t);
        }
        if (t instanceof IntegrationException ex) {
            return ex;
        }
        return new IntegrationException(defaultMessage, null, t);
    }
}
