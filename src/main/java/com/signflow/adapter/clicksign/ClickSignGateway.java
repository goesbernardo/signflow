package com.signflow.adapter.clicksign;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.clicksign.client.ClickSignIntegrationFeignClient;
import com.signflow.adapter.clicksign.dto.*;
import com.signflow.adapter.clicksign.mapper.ClickSignMapper;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
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

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public Envelope createEnvelope(CreateEnvelopeCommand cmd) {
        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> clickSignBody = ClickSignRequestApiDTO.of("envelopes", new ClickSignEnvelopeAttributesDTO(cmd.getName()));
        var response = clickSignClient.createEnvelope(clickSignBody);
        return mapper.toDomain(response);
    }

    public Envelope createEnvelopeFallback(CreateEnvelopeCommand cmd, Throwable t) {
        log.error("Erro ao criar envelope na ClickSign (fallback): {}", t.getMessage());
        return Envelope.builder()
                .name(cmd.getName())
                .build();
    }

    @Override
    public Envelope getEnvelope(String externalId) {
        return mapper.toDomain(clickSignClient.getEnvelope(externalId));
    }

    @Override
    public Signer addSigner(String envelopeId, AddSignerCommand cmd) {
        ClickSignCreateSignAttributesDTO attributes = new ClickSignCreateSignAttributesDTO();
        attributes.setName(cmd.getName());
        attributes.setEmail(cmd.getEmail());
        attributes.setDocumentation(cmd.getDocumentation());
        attributes.setHasDocumentation(cmd.getHasDocumentation());

        ClickSignCreateSignEventsDTO events = new ClickSignCreateSignEventsDTO();
        events.setSignatureRequest("email"); // Valor padrao para o fluxo
        attributes.setCommunicateEvents(events);

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("signers", attributes);
        return mapper.toSignerDomain(clickSignClient.createSigner(envelopeId, body));
    }

    @Override
    public Document addDocument(String envelopeId, AddDocumentCommand cmd) {
        ClickSignCreateDocumentAttributesDTO attributes = new ClickSignCreateDocumentAttributesDTO();
        attributes.setFilename(cmd.getFilename());
        attributes.setContentBase64(cmd.getContentBase64());

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("documents", attributes);
        return mapper.toDocumentDomain(clickSignClient.createDocument(envelopeId, body));
    }

    @Override
    public void addRequirement(String envelopeId, AddRequirementCommand cmd) {
        ClickSignRequirementsAttributesDTO attributes = new ClickSignRequirementsAttributesDTO();
        attributes.setAction(cmd.getAction() != null ? cmd.getAction() : "sign");
        attributes.setRole(cmd.getRole() != null ? cmd.getRole() : "signer");

        ClickSignRequirementsRelationshipDTO relationships = new ClickSignRequirementsRelationshipDTO();
        relationships.setDocument(new RelationshipDataDTO(new DataIdDTO("documents", cmd.getDocumentId())));
        relationships.setSigner(new RelationshipDataDTO(new DataIdDTO("signers", cmd.getSignerId())));

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO, ClickSignRequirementsRelationshipDTO>> body = ClickSignRequestApiDTO.of("requirements", attributes, relationships);
        clickSignClient.createRequirements(envelopeId, body);
    }

    @Override
    public void activateEnvelope(String envelopeId) {
        ClicksignActivateAttributesDTO attributes = new ClicksignActivateAttributesDTO();
        attributes.setStatus("active");
        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("envelopes", attributes);
        clickSignClient.activateEnvelope(envelopeId, body);
    }

    @Override
    public ProviderSignature provider() {
        return ProviderSignature.CLICKSIGN;
    }
}
