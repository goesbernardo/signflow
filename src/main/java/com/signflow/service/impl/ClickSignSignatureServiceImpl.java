package com.signflow.service.impl;

import com.signflow.dto.clicksign.*;
import com.signflow.exception.clicksign.InvalidRequestException;
import com.signflow.factory.SignatureProvider;
import com.signflow.service.ClickSignSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickSignSignatureServiceImpl implements ClickSignSignatureService {

    private final SignatureProvider signatureProviderStrategy;

    @Override
    public SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO request) {
        return signatureProviderStrategy.createEnvelope(request);
    }

    @Override
    public SignatureClickSignListResponseSignersDTO getEnvelope(String envelopeId) {
        return signatureProviderStrategy.getEnvelope(envelopeId);
    }

    @Override
    public SignatureClickSignSignerResponseDTO createSigner(String envelopeId, ClickSignCreateSignerRequestDTO request) {
        return signatureProviderStrategy.createSigner(envelopeId, request);
    }

    @Override
    public SignatureClickSignSignerResponseDTO getSigner(String envelopeId, String signerId) {
        return signatureProviderStrategy.getSigner(envelopeId, signerId);
    }

    @Override
    public SignatureClickSignDocumentListResponseDTO getDocuments(String envelopeId) {
        return signatureProviderStrategy.getDocuments(envelopeId);
    }

    @Override
    public SignatureClickSignDocumentResponseDTO createDocument(String envelopeId, ClickSignCreateDocumentDTO request) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar documento.");
        }
        return signatureProviderStrategy.createDocument(envelopeId, request);
    }

    @Override
    public SignatureClickSignDocumentListResponseDTO updateDocuments(String envelopeId, String id) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para atualizar documento.");
        }

        if (id == null || id.isBlank()) {
            throw new InvalidRequestException("O id do documento é obrigatório para atualização.");
        }

        return signatureProviderStrategy.updateDocuments(envelopeId, id);
    }

    @Override
    public SignatureClickSignRequirementResponseDTO getRequirements(String envelopeId) {
        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar requisitos.");
        }

        return signatureProviderStrategy.getRequirements(envelopeId);
    }
}
