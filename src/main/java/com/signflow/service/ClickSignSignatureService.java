package com.signflow.service;

import com.signflow.dto.ClickSignWebhookRequestDTO;
import com.signflow.dto.ClickSignWebhookResponseDTO;
import com.signflow.dto.clicksign.*;


public interface ClickSignSignatureService {

    ClickSignWebhookResponseDTO createWebhook(ClickSignWebhookRequestDTO request);

    SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO request);

    SignatureClickSignGetResponseDTO getEnvelopeById(String envelopeId);

    SignatureClickSignUpdateResponseDTO updateEnvelope(String envelopeId, ClickSignUpdateEnvelopeRequestDTO request);

    SignatureClickSignListResponseSignersDTO getEnvelope(String envelopeId);

    SignatureClickSignSignerResponseDTO createSigner(String envelopeId, ClickSignCreateSignerRequestDTO request);

    SignatureClickSignSignerResponseDTO getSigner(String envelopeId, String signerId);

    SignatureClickSignDocumentListResponseDTO getDocuments(String envelopeId);

    SignatureClickSignDocumentResponseDTO createDocument(String envelopeId, ClickSignCreateDocumentDTO request);

    SignatureClickSignDocumentListResponseDTO updateDocuments(String envelopeId, String id);

    SignatureClickSignRequirementResponseDTO getRequirements(String envelopeId);

    SignatureClickSignRequirementResponseDTO createRequirements(String envelopeId, ClickSignCreateRequestQualifierAttributesDTO request);








}
