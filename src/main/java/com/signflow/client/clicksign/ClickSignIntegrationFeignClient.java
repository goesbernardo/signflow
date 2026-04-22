package com.signflow.client.clicksign;

import com.signflow.config.feign.ClickSignFeignConfig;
import com.signflow.dto.clicksign.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "clicksign-client", url = "${clicksign.api.url}", configuration = ClickSignFeignConfig.class)
public interface ClickSignIntegrationFeignClient {

    /**
     * Cria um novo envelope na ClickSign.
     */
    @PostMapping(value = "envelopes")
    SignatureClickSignResponseDTO createEnvelope(@RequestBody ClickSignCreateEnvelopeRequestDTO request);

    /**
     * Edita um envelope na ClickSign.
     */
    @PatchMapping(value = "envelopes/{envelopeId}")
    SignatureClickSignUpdateResponseDTO updateEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignUpdateEnvelopeRequestDTO request);

    /**
     * Lista um envelope na ClickSign.
     */
    @GetMapping(value = "envelopes/{envelopeId}")
    SignatureClickSignGetResponseDTO getEnvelope(@PathVariable String envelopeId);

    /**
     * Lista os signatarios vinculados a um envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/signers")
    SignatureClickSignListResponseSignersDTO getEnvelopeSigners(@PathVariable String envelopeId);

    /**
     * Consulta os detalhes de um signatario especifico do envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/signers/{id}/")
    SignatureClickSignSignerResponseDTO getSigner(@PathVariable String envelopeId, @PathVariable String id);

    /**
     * Cria um novo signatario para o envelope informado.
     */
    @PostMapping(value = "envelopes/{envelopeId}/signers")
    SignatureClickSignSignerResponseDTO createSigner(@PathVariable String envelopeId, @RequestBody ClickSignCreateSignerRequestDTO request);

    /**
     * Lista os documentos associados ao envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/documents")
    SignatureClickSignDocumentListResponseDTO getDocuments(@PathVariable String envelopeId);

    /**
     * Cria um documento por upload (base64) em um envelope.
     */
    @PostMapping(value = "envelopes/{envelopeId}/documents")
    SignatureClickSignDocumentResponseDTO createDocument(@PathVariable String envelopeId, @RequestBody ClickSignCreateDocumentDTO request);

    /**
     * Atualiza os dados de um documento especifico do envelope.
     */
    @PatchMapping(value = "envelopes/{envelopeId}/documents/{id}")
    SignatureClickSignDocumentListResponseDTO updateDocuments(@PathVariable String envelopeId, @PathVariable String id);

    /**
     * Lista os requisitos de assinatura configurados no envelope.
     */
    @GetMapping(value = "envelopes/{envelopeId}/requirements")
    SignatureClickSignRequirementResponseDTO getRequirements(@PathVariable String envelopeId);

    /**
     * Cria um requisito do tipo qualificador para o envelope.
     */
    @PostMapping(value = "envelopes/{envelopeId}/requirements")
    SignatureClickSignRequirementResponseDTO createRequirementsQualifier(@PathVariable String envelopeId, @RequestBody ClickSignCreateRequestQualifierAttributesDTO request);
}
