package com.signflow.adapter.clicksign;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.adapter.clicksign.exception.ClickSignIntegrationException;
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

        ClickSignCreateSignEventsDTO events = ClickSignCreateSignEventsDTO.builder()
                .signatureRequest(cmd.requestSignature() != null ? cmd.requestSignature() : "email")
                .signatureReminder("none")
                .documentSigned(cmd.delivery() != null ? cmd.delivery() : "email")
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

    // ── addRequirement ────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addRequirementFallback")
    public Requirement addRequirement(String envelopeId, AddRequirementCommand cmd) {
        ClickSignRequirementsAttributesDTO attributes = ClickSignRequirementsAttributesDTO.builder()
                .action(cmd.action())
                .auth(cmd.auth())
                .rubricPages(cmd.rubricPages())
                .role(cmd.role())
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
        return mapper.toRequirementDomain(clickSignClient.createRequirements(envelopeId, body));
    }

    private Requirement addRequirementFallback(String envelopeId, AddRequirementCommand cmd, Throwable t) {
        log.error("Fallback addRequirement — ClickSign: {}", t.getMessage());
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
            List<ErroDetail> details = clickSignEx.getErrors() == null ? null : clickSignEx.getErrors().stream()
                    .map(error -> ErroDetail.builder()
                            .code(error.getCode())
                            .message(error.getDetail())
                            .field(error.getSource() != null ? error.getSource().getPointer() : null)
                            .build())
                    .toList();
            return new IntegrationException(defaultMessage, clickSignEx.getRawResponse(), details, t);
        }
        if (t instanceof IntegrationException) {
            return (IntegrationException) t;
        }
        return new IntegrationException(defaultMessage, null, t);
    }

    // ── provider ──────────────────────────────────────────────────────────────

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.CLICKSIGN;
    }
}
