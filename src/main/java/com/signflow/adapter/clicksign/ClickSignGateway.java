package com.signflow.adapter.clicksign;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.adapter.clicksign.exception.ClickSignIntegrationException;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.RequirementAction;
import com.signflow.enums.RequirementAuth;
import com.signflow.exception.domain.ErroDetail;
import com.signflow.exception.domain.IntegrationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "signflow.providers.clicksign", name = "enabled", havingValue = "true")
public class ClickSignGateway implements ESignatureGateway {

    private final ClickSignIntegrationFeignClient clickSignClient;
    private final ClickSignMapper mapper;
    private static final String ERR_UNAVAILABLE   = "Serviço ClickSign indisponível no momento. Tente novamente em instantes.";
    private static final String ERR_FETCH_FAILED  = "Não foi possível consultar o envelope na ClickSign.";
    private static final String ERR_DOC_FETCH_FAILED  = "Não foi possível consultar documentos na ClickSign.";

    // ── createEnvelope ────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public Envelope createEnvelope(CreateEnvelopeCommand cmd) {
        var body = ClickSignRequestApiDTO.of("envelopes", ClickSignEnvelopeAttributesDTO.builder().name(cmd.name()).build());
        log.info("Enviando requisição para ClickSign: {}", body);
        var response = clickSignClient.createEnvelope(body);
        log.info("Resposta recebida da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope createEnvelopeFallback(CreateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback createEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── updateEnvelope ────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "updateEnvelopeFallback")
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd) {
        var body = ClickSignRequestApiDTO.of("envelopes", ClickSignEnvelopeAttributesDTO.builder().name(cmd.name()).build());
        log.info("Enviando atualização para ClickSign: {}", body);
        var response = clickSignClient.updateEnvelope(externalId, body);
        log.info("Resposta de atualização da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope updateEnvelopeFallback(String externalId, UpdateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback updateEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── getEnvelope ───────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getEnvelopeFallback")
    public Envelope getEnvelope(String externalId) {
        return mapper.toEnvelopeDomain(clickSignClient.getEnvelope(externalId));
    }

    private Envelope getEnvelopeFallback(String externalId, Throwable t) {
        log.error("Fallback getEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t, ERR_FETCH_FAILED);
    }

    // ── addSigners ────────────────────────────────────────────────────────────
    @Override
    public List<Signer> addSigners(String envelopeId, List<AddSignerCommand> commands) {
        return commands.stream()
                .map(cmd -> this.addSignerInternal(envelopeId, cmd))
                .toList();
    }

    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addSignerFallback")
    private Signer addSignerInternal(String envelopeId, AddSignerCommand cmd) {
        String documentation = cmd.documentation();
        String signatureRequest = cmd.delivery() != null ? cmd.delivery() : "email";
        String documentSigned = "sms".equalsIgnoreCase(signatureRequest) ? "email" : signatureRequest;

        ClickSignCreateSignEventsDTO events = ClickSignCreateSignEventsDTO.builder()
                .signatureRequest(signatureRequest)
                .signatureReminder("none")
                .documentSigned(documentSigned) // ← fallback para email quando delivery = sms
                .build();

        ClickSignCreateSignAttributesDTO attributes = ClickSignCreateSignAttributesDTO.builder()
                .name(cmd.name() != null ? cmd.name().trim() : null)
                .email(cmd.email())
                .group("1")
                .documentation(documentation)
                .hasDocumentation(cmd.hasDocumentation() != null
                        ? cmd.hasDocumentation()
                        : (documentation != null && !documentation.isEmpty()))
                .refusable(false)
                .locationRequiredEnabled(false)
                .phoneNumber(cmd.phoneNumber())
                .communicateEvents(events)
                .build();

        var body = ClickSignRequestApiDTO.of("signers", attributes);
        log.info("Adicionando signatário ao envelope {}: {}", envelopeId, body);
        var response = clickSignClient.createSigner(envelopeId, body);
        log.info("Resposta do signatário: {}", response);
        return mapper.toSignerDomain(response);
    }

    private Signer addSignerFallback(String envelopeId, AddSignerCommand cmd, Throwable t) {
        log.error("Fallback addSigner — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getSignersFallback")
    public List<Signer> getSigners(String envelopeId) {
        log.info("Consultando signatários do envelope {} na ClickSign", envelopeId);
        return mapper.toSignerListDomain(clickSignClient.getEnvelopeSigners(envelopeId));
    }

    private List<Signer> getSignersFallback(String envelopeId, Throwable t) {
        log.error("Fallback getSigners — ClickSign: {}", t.getMessage());
        throw translateException(t, "Erro ao buscar signatários do envelope.");
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getSignerFallback")
    public Signer getSigner(String envelopeId, String signerId) {
        log.info("Consultando signatário {} do envelope {} na ClickSign", signerId, envelopeId);
        return mapper.toSignerDomain(clickSignClient.getSigner(envelopeId, signerId));
    }

    private Signer getSignerFallback(String envelopeId, String signerId, Throwable t) {
        log.error("Fallback getSigner — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "deleteSignerFallback")
    public void deleteSigner(String envelopeId, String signerId) {
        log.info("Excluindo signatário {} do envelope {} na ClickSign", signerId, envelopeId);
        clickSignClient.deleteSigner(envelopeId, signerId);
    }

    private void deleteSignerFallback(String envelopeId, String signerId, Throwable t) {
        log.error("Fallback deleteSigner — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addDocument ───────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addDocumentFallback")
    public Document addDocument(String envelopeId, AddDocumentCommand cmd) {
        ClickSignCreateDocumentAttributesDTO attributes = ClickSignCreateDocumentAttributesDTO.builder()
                .filename(cmd.filename())
                .contentBase64(cmd.contentBase64())
                .build();

        var body = ClickSignRequestApiDTO.of("documents", attributes);
        return mapper.toDocumentDomain(clickSignClient.createDocument(envelopeId, body));
    }

    private Document addDocumentFallback(String envelopeId, AddDocumentCommand cmd, Throwable t) {
        log.error("Fallback addDocument — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getDocumentsFallback")
    public List<Document> getDocuments(String envelopeId) {
        log.info("Consultando documentos do envelope {} na ClickSign", envelopeId);
        return mapper.toDocumentListDomain(clickSignClient.getDocuments(envelopeId));
    }

    private List<Document> getDocumentsFallback(String envelopeId, Throwable t) {
        log.error("Fallback getDocuments — ClickSign: {}", t.getMessage());
        throw translateException(t, ERR_DOC_FETCH_FAILED);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getDocumentFallback")
    public Document getDocument(String documentId) {
        log.info("Consultando documento {} na ClickSign", documentId);
        return mapper.toDocumentDomain(clickSignClient.getDocument(documentId));
    }

    private Document getDocumentFallback(String documentId, Throwable t) {
        log.error("Fallback getDocument — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "updateDocumentFallback")
    public Document updateDocument(String documentId, UpdateDocumentCommand cmd) {
        var attributes = ClickSignCreateDocumentAttributesDTO.builder()
                .filename(cmd.filename())
                .build();
        var body = ClickSignRequestApiDTO.of("documents", attributes);
        log.info("Atualizando documento {} na ClickSign: {}", documentId, body);
        return mapper.toDocumentDomain(clickSignClient.updateDocument(documentId, body));
    }

    private Document updateDocumentFallback(String documentId, UpdateDocumentCommand cmd, Throwable t) {
        log.error("Fallback updateDocument — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "deleteDocumentFallback")
    public void deleteDocument(String documentId) {
        log.info("Excluindo documento {} na ClickSign", documentId);
        clickSignClient.deleteDocument(documentId);
    }

    private void deleteDocumentFallback(String documentId, Throwable t) {
        log.error("Fallback deleteDocument — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── addRequirement ────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addRequirementFallback")
    public Requirement addRequirement(String envelopeId, AddRequirementCommand cmd) {

        // auth só vai quando action = PROVIDE_EVIDENCE
        RequirementAuth auth = RequirementAction.PROVIDE_EVIDENCE.equals(cmd.action())
                ? cmd.auth() : null;

        // rubric_pages só vai quando action = AGREE ou RUBRICATE
        String rubricPages = (RequirementAction.AGREE.equals(cmd.action()) || RequirementAction.RUBRICATE.equals(cmd.action()))
                ? cmd.rubricPages() : null;

        log.info("=== REQUIREMENT REQUEST ===");
        log.info("EnvelopeId: {}", envelopeId);
        log.info("Action: {}", cmd.action());
        log.info("Auth: {}", auth);
        log.info("Role: {}", cmd.role());
        log.info("RubricPages: {}", rubricPages);
        log.info("SignerId: {}", cmd.signerId());
        log.info("DocumentId: {}", cmd.documentId());
        log.info("===========================");

        ClickSignRequirementsAttributesDTO attributes = ClickSignRequirementsAttributesDTO.builder()
                .action(cmd.action())
                .auth(auth)
                .role(cmd.role())
                .rubricPages(rubricPages)
                .build();

        ClickSignRequirementsRelationshipDTO relationships = ClickSignRequirementsRelationshipDTO.builder()
                .document(RelationshipDataDTO.builder()
                        .data(DataIdDTO.builder().type("documents").id(cmd.documentId()).build())
                        .build())
                .signer(RelationshipDataDTO.builder()
                        .data(DataIdDTO.builder().type("signers").id(cmd.signerId()).build())
                        .build())
                .build();

        var body = ClickSignRequestApiDTO.of("requirements", attributes, relationships);
        log.info("Criando requisito no envelope {}: action={}, auth={}, role={}, rubric_pages={}",
                envelopeId, cmd.action(), auth, cmd.role(), rubricPages);
        return mapper.toRequirementDomain(clickSignClient.createRequirements(envelopeId, body));
    }

    private Requirement addRequirementFallback(String envelopeId, AddRequirementCommand cmd, Throwable t) {
        log.error("Fallback addRequirement — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getRequirementsFallback")
    public List<Requirement> getRequirements(String envelopeId) {
        log.info("Buscando requisitos do envelope {}", envelopeId);
        return mapper.toRequirementListDomain(clickSignClient.getRequirements(envelopeId));
    }

    private List<Requirement> getRequirementsFallback(String envelopeId, Throwable t) {
        log.error("Fallback getRequirements — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getRequirementFallback")
    public Requirement getRequirement(String requirementId) {
        log.info("Buscando requisito {}", requirementId);
        return mapper.toRequirementDomain(clickSignClient.getRequirement(requirementId));
    }

    private Requirement getRequirementFallback(String requirementId, Throwable t) {
        log.error("Fallback getRequirement — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "deleteRequirementFallback")
    public void deleteRequirement(String requirementId) {
        log.info("Excluindo requisito {}", requirementId);
        clickSignClient.deleteRequirement(requirementId);
    }

    private void deleteRequirementFallback(String requirementId, Throwable t) {
        log.error("Fallback deleteRequirement — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── activateEnvelope ──────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "activateEnvelopeFallback")
    public void activateEnvelope(String envelopeId) {
        var attributes = ClicksignActivateAttributesDTO.builder()
                .status("running")
                .build();
        var body = ClickSignRequestApiDTO.of(envelopeId, "envelopes", attributes); // ← com id
        log.info("Ativando envelope na ClickSign: {}", body);
        clickSignClient.activateEnvelope(envelopeId, body);
    }

    private void activateEnvelopeFallback(String envelopeId, Throwable t) {
        log.error("Fallback activateEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    private RuntimeException translateException(Throwable t) {
        return translateException(t, ERR_UNAVAILABLE);
    }

    private RuntimeException translateException(Throwable t, String defaultMessage) {
        if (t instanceof ClickSignIntegrationException clickSignEx) {
            List<ErroDetail> details = extractDetails(clickSignEx);
            String message = (clickSignEx.getMessage() != null && !clickSignEx.getMessage().isBlank())
                    ? clickSignEx.getMessage()
                    : defaultMessage;
            return new IntegrationException(message, clickSignEx.getRawResponse(), details, t);
        }
        if (t instanceof IntegrationException integrationEx) {
            return integrationEx;
        }
        return new IntegrationException(defaultMessage, null, t);
    }

    private List<ErroDetail> extractDetails(ClickSignIntegrationException ex) {
        if (ex.getErrors() == null || ex.getErrors().isEmpty()) {
            return List.of();
        }
        return ex.getErrors().stream()
                .map(error -> ErroDetail.builder()
                        .code(error.getCode())
                        .message(error.getDetail())
                        .field(error.getSource() != null ? error.getSource().getPointer() : null)
                        .build())
                .toList();
    }

    // ── provider ──────────────────────────────────────────────────────────────

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.CLICKSIGN;
    }
}
