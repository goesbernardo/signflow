package com.signflow.factory;


import com.signflow.dto.clicksign.*;

public interface SignatureProvider{

    SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO clickSignCreateEnvelopeRequestDTO);
    SignatureClickSignListResponseSignersDTO getEnvelope(String envelopeId);
    SignatureClickSignSignerResponseDTO createSigner(String envelopeId, ClickSignCreateSignerRequestDTO clickSignCreateSignerRequestDTO);
    SignatureClickSignSignerResponseDTO getSigner(String envelopeId, String signerId);
    SignatureClickSignDocumentListResponseDTO getDocuments(String envelopeId);
    SignatureClickSignDocumentResponseDTO createDocument(String envelopeId, ClickSignCreateDocumentDTO request);
    SignatureClickSignDocumentListResponseDTO updateDocuments(String envelopeId, String id);
    SignatureClickSignRequirementResponseDTO getRequirements(String envelopeId);





}
