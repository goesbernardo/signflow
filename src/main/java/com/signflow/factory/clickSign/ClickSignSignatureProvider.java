package com.signflow.factory.clickSign;

import com.signflow.client.clicksign.ClickSignIntegrationFeignClient;
import com.signflow.dto.clicksign.request.*;
import com.signflow.dto.clicksign.response.SignatureClickSignResponseDTO;
import com.signflow.entity.EnvelopeEntity;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.factory.SignatureProvider;
import com.signflow.repository.ClickSignDocumentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickSignSignatureProvider implements SignatureProvider {

    private final ClickSignIntegrationFeignClient clickSignClient;
    private final ClickSignDocumentRepository clickSignDocumentRepository;


    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public SignatureClickSignResponseDTO createEnvelope(ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body) {
        EnvelopeEntity envelopeEntity = new EnvelopeEntity();
        envelopeEntity.setStatus(Status.PROCESSING);
        envelopeEntity.setProvider(ProviderSignature.CLICKSIGN);
        envelopeEntity.setCreated(LocalDateTime.now());

        envelopeEntity = clickSignDocumentRepository.save(envelopeEntity);


        var response = clickSignClient.createEnvelope(body);
        envelopeEntity.setStatus(Status.SUCCESS);
        envelopeEntity.setExternalId(response.getData().getId());
    //    envelopeEntity.setUserId(createEnvelopeRequestDTO.getId());
        clickSignDocumentRepository.save(envelopeEntity);
        return response;
    }

    @Override
    public SignatureClickSignResponseDTO updateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body) {
        return clickSignClient.updateEnvelope(envelopeId, body);
    }

    @Override
    public SignatureClickSignResponseDTO getEnvelopeById(String envelopeId) {
        return clickSignClient.getEnvelope(envelopeId);
    }

    @Override
    public SignatureClickSignResponseDTO getEnvelope(String envelopeId) {
        return clickSignClient.getEnvelopeSigners(envelopeId);
        }


    @Override
    public SignatureClickSignResponseDTO createSigner(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO,Void>> request) {
        return clickSignClient.createSigner(envelopeId, request);
        }


    @Override
    public SignatureClickSignResponseDTO getSigner(String envelopeId, String signerId) {
        return clickSignClient.getSigner(envelopeId, signerId);
        }


    @Override
    public SignatureClickSignResponseDTO getDocuments(String envelopeId) {
        return clickSignClient.getDocuments(envelopeId);
        }


    @Override
    public SignatureClickSignResponseDTO createDocument(String envelopeId,ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO,Void>> request) {
        return clickSignClient.createDocument(envelopeId, request);
        }

    @Override
    public SignatureClickSignResponseDTO updateDocuments(String envelopeId, String id) {
        return clickSignClient.updateDocuments(envelopeId, id);
    }

    @Override
    public SignatureClickSignResponseDTO getRequirements(String envelopeId) {
        return clickSignClient.getRequirements(envelopeId);
        }


    @Override
    public SignatureClickSignResponseDTO createRequirements(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO,ClickSignRequirementsRelationshipDTO>> request) {
        return clickSignClient.createRequirements(envelopeId, request);
    }

    @Override
    public SignatureClickSignResponseDTO activateEnvelope(String envelopeId, ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO, Void>> request) {
        return clickSignClient.activateEnvelope(envelopeId, request);
    }

}
