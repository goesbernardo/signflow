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
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "signflow.providers.clicksign", name = "enabled", havingValue = "true")
public class ClickSignGateway implements ESignatureGateway {

    private final ClickSignIntegrationFeignClient clickSignClient;
    private final ClickSignMapper mapper;
    private final org.springframework.context.MessageSource messageSource;

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public Envelope createEnvelope(CreateEnvelopeCommand cmd) {
        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> clickSignBody = ClickSignRequestApiDTO.of("envelopes", ClickSignEnvelopeAttributesDTO.builder().name(cmd.name()).build());
        log.info("Enviando requisição para ClickSign: {}", clickSignBody);
        var response = clickSignClient.createEnvelope(clickSignBody);
        log.info("Resposta recebida da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope createEnvelopeFallback(CreateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback acionado para createEnvelope devido a error na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "updateEnvelopeFallback")
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd) {
        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> clickSignBody = ClickSignRequestApiDTO.of("envelopes", ClickSignEnvelopeAttributesDTO.builder().name(cmd.name()).build());
        log.info("Enviando requisição de atualização para ClickSign: {}", clickSignBody);
        var response = clickSignClient.updateEnvelope(externalId, clickSignBody);
        log.info("Resposta de atualização recebida da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    private Envelope updateEnvelopeFallback(String externalId, UpdateEnvelopeCommand cmd, Throwable t) {
        log.error("Fallback acionado para updateEnvelope devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "getEnvelopeFallback")
    public Envelope getEnvelope(String externalId) {
        return mapper.toEnvelopeDomain(clickSignClient.getEnvelope(externalId));
    }

    private Envelope getEnvelopeFallback(String externalId, Throwable t) {
        log.error("Fallback acionado para getEnvelope devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_fetch_failed"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addSignerFallback")
    public Signer addSigner(String envelopeId, AddSignerCommand cmd) {
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
                .hasDocumentation(cmd.hasDocumentation() != null ? cmd.hasDocumentation() : (documentation != null && !documentation.isEmpty()))
                .refusable(false)
                .locationRequiredEnabled(false)
                .communicateEvents(events)
                .build();

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("signers", attributes);
        log.info("Adicionando signatário ao envelope {}: {}", envelopeId, body);
        var response = clickSignClient.createSigner(envelopeId, body);
        log.info("Resposta do signatário recebida: {}", response);
        return mapper.toSignerDomain(response);
    }

    private Signer addSignerFallback(String envelopeId, AddSignerCommand cmd, Throwable t) {
        log.error("Fallback acionado para addSigner devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addDocumentFallback")
    public Document addDocument(String envelopeId, AddDocumentCommand cmd) {
        ClickSignCreateDocumentAttributesDTO attributes = ClickSignCreateDocumentAttributesDTO.builder()
                .filename(cmd.filename())
                .contentBase64(cmd.contentBase64())
                .build();

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("documents", attributes);
        return mapper.toDocumentDomain(clickSignClient.createDocument(envelopeId, body));
    }

    private Document addDocumentFallback(String envelopeId, AddDocumentCommand cmd, Throwable t) {
        log.error("Fallback acionado para addDocument devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "addRequirementFallback")
    public Requirement addRequirement(String envelopeId, AddRequirementCommand cmd) {
        ClickSignRequirementsAttributesDTO attributes = ClickSignRequirementsAttributesDTO.builder()
                .action(cmd.action() != null ? cmd.action() : "sign")
                .build();

        ClickSignRequirementsRelationshipDTO relationships = ClickSignRequirementsRelationshipDTO.builder()
                .document(RelationshipDataDTO.builder().data(DataIdDTO.builder().type("documents").id(cmd.documentId()).build()).build())
                .signer(RelationshipDataDTO.builder().data(DataIdDTO.builder().type("signers").id(cmd.signerId()).build()).build())
                .build();

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO, ClickSignRequirementsRelationshipDTO>> body = ClickSignRequestApiDTO.of("requirements", attributes, relationships);
        var response = clickSignClient.createRequirements(envelopeId, body);
        return mapper.toRequirementDomain(response);
    }

    private Requirement addRequirementFallback(String envelopeId, AddRequirementCommand cmd, Throwable t) {
        log.error("Fallback acionado para addRequirement devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "activateEnvelopeFallback")
    public void activateEnvelope(String envelopeId) {
        ClicksignActivateAttributesDTO attributes = ClicksignActivateAttributesDTO.builder()
                .status("active")
                .build();
        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("envelopes", attributes);
        clickSignClient.activateEnvelope(envelopeId, body);
    }

    private void activateEnvelopeFallback(String envelopeId, Throwable t) {
        log.error("Fallback acionado para activateEnvelope devido a erro na ClickSign: {}", t.getMessage());
        throw new com.signflow.exception.domain.IntegrationException(getMessage("error.clicksign_unavailable"), null);
    }

    private String getMessage(String code) {
        return messageSource.getMessage(code, null, org.springframework.context.i18n.LocaleContextHolder.getLocale());
    }

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.CLICKSIGN;
    }
}
