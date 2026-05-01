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
        log.info("Enviando requisição para ClickSign: {}", clickSignBody);
        var response = clickSignClient.createEnvelope(clickSignBody);
        log.info("Resposta recebida da ClickSign: {}", response);
        return mapper.toEnvelopeDomain(response);
    }

    @Override
    public Envelope getEnvelope(String externalId) {
        return mapper.toEnvelopeDomain(clickSignClient.getEnvelope(externalId));
    }

    @Override
    public Signer addSigner(String envelopeId, AddSignerCommand cmd) {
        ClickSignCreateSignAttributesDTO attributes = new ClickSignCreateSignAttributesDTO();
        attributes.setName(cmd.getName() != null ? cmd.getName().trim() : null);
        attributes.setEmail(cmd.getEmail());
        attributes.setGroup("1");
        
        String documentation = cmd.getDocumentation();
        attributes.setDocumentation(documentation);
        
        attributes.setHasDocumentation(cmd.getHasDocumentation() != null ? cmd.getHasDocumentation() : (documentation != null && !documentation.isEmpty()));
        attributes.setRefusable(false);
        attributes.setLocationRequiredEnabled(false);

        ClickSignCreateSignEventsDTO events = new ClickSignCreateSignEventsDTO();
        events.setSignatureRequest(cmd.getRequestSignature() != null ? cmd.getRequestSignature() : "email"); // Valor padrao para o fluxo
        events.setSignatureReminder("none");
        events.setDocumentSigned(cmd.getDelivery() != null ? cmd.getDelivery() : "email");
        attributes.setCommunicateEvents(events);

        ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO, Void>> body = ClickSignRequestApiDTO.of("signers", attributes);
        log.info("Adicionando signatário ao envelope {}: {}", envelopeId, body);
        var response = clickSignClient.createSigner(envelopeId, body);
        log.info("Resposta do signatário recebida: {}", response);
        return mapper.toSignerDomain(response);
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
