package com.signflow.adapter.clicksign.gateway;

import com.signflow.gateway.ESignatureGateway;
import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.adapter.clicksign.exception.ClickSignIntegrationException;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.*;
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

    private static final String ERR_UNAVAILABLE  = "Provedor indisponível";
    private static final String ERR_FETCH_FAILED = "Não foi possível consultar";

    // ── createEnvelope ────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public Envelope createEnvelope(CreateEnvelopeCommand cmd) {
        var body = ClickSignRequestApiDTO.of("envelopes",
                ClickSignEnvelopeAttributesDTO.builder()
                        .name(cmd.name())
                        .build());
        log.info("Enviando requisição para ClickSign: {}", body);
        var response = clickSignClient.createEnvelope(body);
        log.info("Resposta recebida da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope createEnvelopeFallback(CreateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback createEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── updateEnvelope ────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "updateEnvelopeFallback")
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd) {
        var body = ClickSignRequestApiDTO.of("envelopes", ClickSignEnvelopeAttributesDTO.builder()
                        .name(cmd.name())
                        .build());
        log.info("Enviando atualização para ClickSign: {}", body);
        var response = clickSignClient.updateEnvelope(externalId, body);
        log.info("Resposta de atualização da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope updateEnvelopeFallback(String externalId, UpdateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback updateEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── getEnvelope ───────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getEnvelopeFallback")
    public Envelope getEnvelope(String externalId) {
        return mapper.toEnvelopeDomain(clickSignClient.getEnvelope(externalId));
    }

    private Envelope getEnvelopeFallback(String externalId, Throwable t) {
        log.error("Fallback getEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t, ERR_FETCH_FAILED);
    }

    // ── addSigner ─────────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addSignerFallback")
    public Signer addSigner(String envelopeId, AddSignerCommand cmd) {
        String documentation = cmd.documentation();
        String signatureRequest = mapSignatureRequest(cmd.notificationChannel());
        String documentSigned   = mapDocumentSigned(cmd.notificationChannel());

        ClickSignCreateSignEventsDTO events = ClickSignCreateSignEventsDTO.builder()
                .signatureRequest(signatureRequest)
                .signatureReminder("none")
                .documentSigned(documentSigned)
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

    // ── addDocument ───────────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addDocumentFallback")
    public Document addDocument(String envelopeId, AddDocumentCommand cmd) {
        var attributes = ClickSignCreateDocumentAttributesDTO.builder()
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

    // ── addRequirement ────────────────────────────────────────────────────

    /**
     * A ClickSign exige dois tipos de requisito por signatário:
     * <p>
     * 1. Qualificação: action="agree" + role (mapeia SignerRole)
     * 2. Autenticação: action="provide_evidence" + auth (mapeia SignatureAuthMethod)
     * <p>
     * O campo action é determinado internamente pelo gateway baseado
     * no que foi preenchido no command — não é mais exposto para o domínio.
     * Outros providers implementarão sua própria lógica de requisitos.
     */
    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addRequirementFallback")
    public Requirement addRequirement(String envelopeId, AddRequirementCommand cmd) {

        ClickSignRequirementsAttributesDTO attributes;

        if (cmd.role() != null) {
            attributes = ClickSignRequirementsAttributesDTO.builder()
                    .action(RequirementAction.AGREE)
                    .role(mapSignerRole(cmd.role()))
                    .build();

        } else if (cmd.auth() != null) {
            attributes = ClickSignRequirementsAttributesDTO.builder()
                    .action(RequirementAction.PROVIDE_EVIDENCE)
                    .auth(mapAuthMethod(cmd.auth()))
                    .build();

        } else {
            log.warn("AddRequirementCommand sem role nem auth — usando qualificação padrão (agree + sign)");
            attributes = ClickSignRequirementsAttributesDTO.builder()
                    .action(RequirementAction.AGREE)
                    .role(RequirementRole.SIGN)
                    .build();
        }

        var relationships = ClickSignRequirementsRelationshipDTO.builder()
                .document(RelationshipDataDTO.builder()
                        .data(DataIdDTO.builder().type("documents").id(cmd.documentId()).build())
                        .build())
                .signer(RelationshipDataDTO.builder()
                        .data(DataIdDTO.builder().type("signers").id(cmd.signerId()).build())
                        .build())
                .build();

        var body = ClickSignRequestApiDTO.of("requirements", attributes, relationships);
        log.info("Criando requisito no envelope {}: action={}", envelopeId, attributes.action());
        return mapper.toRequirementDomain(clickSignClient.createRequirements(envelopeId, body));
    }

    private Requirement addRequirementFallback(String envelopeId, AddRequirementCommand cmd, Throwable t) {
        log.error("Fallback addRequirement — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── activateEnvelope ──────────────────────────────────────────────────

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "activateEnvelopeFallback")
    public void activateEnvelope(String envelopeId) {
        var attributes = ClicksignActivateAttributesDTO.builder()
                .status("running")
                .build();
        var body = ClickSignRequestApiDTO.of(envelopeId, "envelopes", attributes);
        clickSignClient.activateEnvelope(envelopeId, body);
    }

    private void activateEnvelopeFallback(String envelopeId, Throwable t) {
        log.error("Fallback activateEnvelope — ClickSign: {}", t.getMessage());
        throw translateException(t);
    }

    // ── provider ──────────────────────────────────────────────────────────

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.CLICKSIGN;
    }

    // ── Mapeamento de enums do domínio → Strings da ClickSign ─────────────

    /**
     * Mapeia SignatureAuthMethod (domínio neutro) para o valor
     * aceito pelo campo auth da ClickSign API v3.
     * <p>
     * Quando novos providers forem implementados, cada um terá
     * seu próprio método de mapeamento sem afetar este.
     */
    private RequirementAuth mapAuthMethod(SignatureAuthMethod auth) {
        if (auth == null) return RequirementAuth.EMAIL;
        return switch (auth) {
            case EMAIL             -> RequirementAuth.EMAIL;
            case SMS               -> RequirementAuth.SMS;
            case WHATSAPP          -> RequirementAuth.WHATSAPP;
            case PIX               -> RequirementAuth.PIX;
            case HANDWRITTEN       -> RequirementAuth.HANDWRITTEN;
            case FACIAL_BIOMETRICS -> RequirementAuth.FACIAL_BIOMETRICS;
            case API               -> RequirementAuth.API;
            case AUTO              -> RequirementAuth.AUTO_SIGNATURE;
        };
    }

    /**
     * Mapeia SignerRole (domínio neutro) para o valor
     * aceito pelo campo role da ClickSign API v3.
     */
    private RequirementRole mapSignerRole(SignerRole role) {
        if (role == null) return RequirementRole.SIGN;
        return switch (role) {
            case SIGN -> RequirementRole.SIGN;
            case PARTY -> RequirementRole.RECEIPT;      // mais próximo semânticamente
            case CONTRACTOR -> RequirementRole.CONTRACTOR;
            case WITNESS -> RequirementRole.ATTORNEY;     // sem WITNESS no RequirementRole
            case INTERVENING -> RequirementRole.INTERVENING;
        };
    }

    // ── Tratamento de erros ───────────────────────────────────────────────

    private RuntimeException translateException(Throwable t) {
        return translateException(t, ERR_UNAVAILABLE);
    }

    private RuntimeException translateException(Throwable t, String defaultMessage) {
        if (t instanceof ClickSignIntegrationException clickSignEx) {
            String message = clickSignEx.getMessage() != null && !clickSignEx.getMessage().isBlank()
                    ? clickSignEx.getMessage()
                    : defaultMessage;

            List<ErroDetail> details = clickSignEx.getErrors() == null ? null
                    : clickSignEx.getErrors().stream()
                      .map(error -> ErroDetail.builder()
                                    .code(error.getCode())
                                    .message(error.getDetail())
                                    .field(error.getSource() != null ? error.getSource().getPointer() : null)
                                    .build())
                      .toList();
            return new IntegrationException(message, clickSignEx.getRawResponse(), details, t);
        }
        if (t instanceof IntegrationException ex) {
            return ex;
        }
        return new IntegrationException(defaultMessage, null, t);
    }

    private String mapSignatureRequest(NotificationChannel channel) {
        if (channel == null) return "email";
        return switch (channel) {
            case EMAIL    -> "email";
            case SMS      -> "sms";
            case WHATSAPP -> "whatsapp";
        };
    }

    private String mapDocumentSigned(NotificationChannel channel) {
        if (channel == null) return "email";
        return switch (channel) {
            case EMAIL, SMS -> "email";
            case WHATSAPP -> "whatsapp";
        };
    }
}
