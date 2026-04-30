package com.signflow.service;

import com.signflow.dto.clicksign.request.*;
import com.signflow.dto.clicksign.response.SignatureClickSignResponseDTO;


public interface ClicksignService {

    SignatureClickSignResponseDTO createEnvelope(ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body);

    SignatureClickSignResponseDTO getEnvelopeById(String envelopeId);

    SignatureClickSignResponseDTO updateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body);

    SignatureClickSignResponseDTO getEnvelope(String envelopeId);

    SignatureClickSignResponseDTO createSigner(String envelopeId,ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO,Void>> request);

    SignatureClickSignResponseDTO getSigner(String envelopeId, String signerId);

    SignatureClickSignResponseDTO getDocuments(String envelopeId);

    SignatureClickSignResponseDTO createDocument(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO,Void>> request);

    SignatureClickSignResponseDTO updateDocuments(String envelopeId, String id);

    SignatureClickSignResponseDTO getRequirements(String envelopeId);

    SignatureClickSignResponseDTO createRequirements(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO,ClickSignRequirementsRelationshipDTO>> request);

    SignatureClickSignResponseDTO activateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO,Void>> request);








}
