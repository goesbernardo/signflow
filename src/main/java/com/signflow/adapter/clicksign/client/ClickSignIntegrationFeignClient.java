package com.signflow.adapter.clicksign.client;

import com.signflow.adapter.clicksign.ClickSignFeignConfig;
import com.signflow.adapter.clicksign.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "clicksign-client", url = "${signflow.providers.clicksign.base-url}", configuration = ClickSignFeignConfig.class)
public interface ClickSignIntegrationFeignClient {

    @PostMapping(value = "envelopes")
    SignatureClickSignResponseDTO createEnvelope(@RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> request);

    /**
     * Edita um envelope na ClickSign.
     */
    @PatchMapping(value = "envelopes/{envelopeId}")
    SignatureClickSignResponseDTO updateEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO,Void>> request);

    /**
     * Lista um envelope na ClickSign.
     */
    @GetMapping(value = "envelopes/{envelopeId}")
    SignatureClickSignResponseDTO getEnvelope(@PathVariable String envelopeId);

    /**
     * Lista os signatarios vinculados a um envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/signers")
    SignatureClickSignResponseDTO getEnvelopeSigners(@PathVariable String envelopeId);

    /**
     * Consulta os detalhes de um signatario especifico do envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/signers/{id}/")
    SignatureClickSignResponseDTO getSigner(@PathVariable String envelopeId, @PathVariable String id);

    /**
     * Cria um novo signatario para o envelope informado.
     */
    @PostMapping(value = "envelopes/{envelopeId}/signers")
    SignatureClickSignResponseDTO createSigner(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO,Void>> request);

    /**
     * Lista os documentos associados ao envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/documents")
    SignatureClickSignResponseDTO getDocuments(@PathVariable String envelopeId);

    /**
     * Cria um documento por upload (base64) em um envelope.
     */
    @PostMapping(value = "envelopes/{envelopeId}/documents")
    SignatureClickSignResponseDTO createDocument(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO,Void>> request);

    /**
     * Atualiza os dados de um documento especifico do envelope.
     */
    @PatchMapping(value = "envelopes/{envelopeId}/documents/{id}")
    SignatureClickSignResponseDTO updateDocuments(@PathVariable String envelopeId, @PathVariable String id);

    /**
     * Lista os requisitos de assinatura configurados no envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/requirements")
    SignatureClickSignResponseDTO getRequirements(@PathVariable String envelopeId);

    /**
     * Cria um requisito do tipo qualificador para o envelope.
     */
    @PostMapping(value = "envelopes/{envelopeId}/requirements")
    SignatureClickSignResponseDTO createRequirements(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO,ClickSignRequirementsRelationshipDTO>> request);

    /**
     * Cria uma ativação  para o envelope.
     */
    @PutMapping("envelopes/{envelopeId}")
    SignatureClickSignResponseDTO activateEnvelope(@PathVariable String envelopeId,@RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO,Void>> request);

    @PostMapping(value = "/acceptance_term/whatsapps", consumes = "application/vnd.api+json", produces = "application/vnd.api+json")
    ClickSignWhatsAppAcceptanceResponseDTO createWhatsAppAcceptance(@RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignWhatsAppAcceptanceAttributesDTO, Void>> request);

}
