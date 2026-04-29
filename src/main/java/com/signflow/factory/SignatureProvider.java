package com.signflow.factory;


import com.signflow.dto.ClickSignWebhookRequestDTO;
import com.signflow.dto.ClickSignWebhookResponseDTO;
import com.signflow.dto.clicksign.*;

public interface SignatureProvider{

    ClickSignWebhookResponseDTO createWebhook(ClickSignWebhookRequestDTO clickSignWebhookRequestDTO);

    SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO clickSignCreateEnvelopeRequestDTO);
    SignatureClickSignUpdateResponseDTO updateEnvelope(String envelopeId, ClickSignUpdateEnvelopeRequestDTO clickSignUpdateEnvelopeRequestDTO);
    SignatureClickSignGetResponseDTO getEnvelopeById(String envelopeId);
    SignatureClickSignListResponseSignersDTO getEnvelope(String envelopeId);
    SignatureClickSignSignerResponseDTO createSigner(String envelopeId, ClickSignCreateSignerRequestDTO clickSignCreateSignerRequestDTO);
    SignatureClickSignSignerResponseDTO getSigner(String envelopeId, String signerId);
    SignatureClickSignDocumentListResponseDTO getDocuments(String envelopeId);
    SignatureClickSignDocumentResponseDTO createDocument(String envelopeId, ClickSignCreateDocumentDTO request);
    SignatureClickSignDocumentListResponseDTO updateDocuments(String envelopeId, String id);
    SignatureClickSignRequirementResponseDTO getRequirements(String envelopeId);
    SignatureClickSignRequirementResponseDTO createRequirements(String envelopeId, ClickSignCreateRequestQualifierAttributesDTO request);






}
