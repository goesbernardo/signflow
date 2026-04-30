package com.signflow.service.impl;

import com.signflow.dto.clicksign.request.*;
import com.signflow.dto.clicksign.response.SignatureClickSignResponseDTO;
import com.signflow.exception.clicksign.InvalidRequestException;
import com.signflow.factory.SignatureProvider;
import com.signflow.service.ClicksignService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClicksignServiceImpl implements ClicksignService {

    private final SignatureProvider signatureProviderStrategy;

    @Override
    public SignatureClickSignResponseDTO createEnvelope(ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> request) {
        return signatureProviderStrategy.createEnvelope(request);
    }

    @Override
    public SignatureClickSignResponseDTO getEnvelopeById(String envelopeId) {
        return signatureProviderStrategy.getEnvelopeById(envelopeId);
    }

    @Override
    public SignatureClickSignResponseDTO updateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body) {
        return signatureProviderStrategy.updateEnvelope(envelopeId, body);
    }

    @Override
    public SignatureClickSignResponseDTO getEnvelope(String envelopeId) {
        return signatureProviderStrategy.getEnvelope(envelopeId);
    }

    @Override
    public SignatureClickSignResponseDTO createSigner(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO,Void>> request) {
        return signatureProviderStrategy.createSigner(envelopeId, request);
    }

    @Override
    public SignatureClickSignResponseDTO getSigner(String envelopeId, String signerId) {
        return signatureProviderStrategy.getSigner(envelopeId, signerId);
    }

    @Override
    public SignatureClickSignResponseDTO getDocuments(String envelopeId) {
        return signatureProviderStrategy.getDocuments(envelopeId);
    }

    @Override
    public SignatureClickSignResponseDTO createDocument(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO,Void>> request) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar documento.");
        }
        return signatureProviderStrategy.createDocument(envelopeId, request);
    }

    @Override
    public SignatureClickSignResponseDTO updateDocuments(String envelopeId, String id) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para atualizar documento.");
        }

        if (id == null || id.isBlank()) {
            throw new InvalidRequestException("O id do documento é obrigatório para atualização.");
        }

        return signatureProviderStrategy.updateDocuments(envelopeId, id);
    }

    @Override
    public SignatureClickSignResponseDTO getRequirements(String envelopeId) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar requisitos.");
        }

        return signatureProviderStrategy.getRequirements(envelopeId);
    }

    @Override
    public SignatureClickSignResponseDTO createRequirements(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO,ClickSignRequirementsRelationshipDTO>> request) {
        return signatureProviderStrategy.createRequirements(envelopeId, request);
    }

    @Override
    public SignatureClickSignResponseDTO activateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO, Void>> request) {
        return signatureProviderStrategy.activateEnvelope(envelopeId, request);
    }
}
