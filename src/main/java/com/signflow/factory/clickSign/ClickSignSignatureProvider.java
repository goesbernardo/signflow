package com.signflow.factory.clickSign;

import com.signflow.client.clicksign.ClickSignIntegrationFeignClient;
import com.signflow.dto.ClickSignWebhookRequestDTO;
import com.signflow.dto.ClickSignWebhookResponseDTO;
import com.signflow.dto.clicksign.*;
import com.signflow.entity.EnvelopeEntity;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.exception.clicksign.ClickSignIntegrationException;
import com.signflow.exception.clicksign.ResourceNotFoundException;
import com.signflow.factory.ClickSignResponseFactory;
import com.signflow.factory.SignatureProvider;
import com.signflow.repository.ClickSignDocumentRepository;
import feign.FeignException;
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
    public ClickSignWebhookResponseDTO createWebhook(ClickSignWebhookRequestDTO clickSignWebhookRequestDTO) {
        return clickSignClient.createWebhook(clickSignWebhookRequestDTO);
    }

    @Override
    @CircuitBreaker(name = "clicksign-circuit-breaker", fallbackMethod = "createEnvelopeFallback")
    public SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO createEnvelopeRequestDTO) {
        EnvelopeEntity envelopeEntity = new EnvelopeEntity();
        envelopeEntity.setStatus(Status.PROCESSING);
        envelopeEntity.setProvider(ProviderSignature.CLICKSIGN);
        envelopeEntity.setCreated(LocalDateTime.now());

        envelopeEntity = clickSignDocumentRepository.save(envelopeEntity);


        var response = clickSignClient.createEnvelope(createEnvelopeRequestDTO);
        envelopeEntity.setStatus(Status.SUCCESS);
        envelopeEntity.setExternalId(response.getData().getId());
        envelopeEntity.setUserId(createEnvelopeRequestDTO.getId());
        clickSignDocumentRepository.save(envelopeEntity);
        return response;
    }

    @Override
    public SignatureClickSignUpdateResponseDTO updateEnvelope(String envelopeId, ClickSignUpdateEnvelopeRequestDTO clickSignUpdateEnvelopeRequestDTO) {
        return clickSignClient.updateEnvelope(envelopeId, clickSignUpdateEnvelopeRequestDTO);
    }

    @Override
    public SignatureClickSignGetResponseDTO getEnvelopeById(String envelopeId) {
        return clickSignClient.getEnvelope(envelopeId);
    }

    @Override
    public SignatureClickSignListResponseSignersDTO getEnvelope(String envelopeId) {
        try {
            return clickSignClient.getEnvelopeSigners(envelopeId);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao consultar signatários do envelope no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignSignerResponseDTO createSigner(String envelopeId, ClickSignCreateSignerRequestDTO clickSignCreateSignerRequestDTO) {
        try {
            return clickSignClient.createSigner(envelopeId, clickSignCreateSignerRequestDTO);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao criar signatário no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignSignerResponseDTO getSigner(String envelopeId, String signerId) {
        try {
            return clickSignClient.getSigner(envelopeId, signerId);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao consultar signatário no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignDocumentListResponseDTO getDocuments(String envelopeId) {
        try {
            return clickSignClient.getDocuments(envelopeId);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao consultar documentos do envelope no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignDocumentResponseDTO createDocument(String envelopeId, ClickSignCreateDocumentDTO request) {
        try {
            return clickSignClient.createDocument(envelopeId, request);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao criar documento por upload no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignDocumentListResponseDTO updateDocuments(String envelopeId, String id) {
        try {
            return clickSignClient.updateDocuments(envelopeId, id);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao atualizar documentos do envelope no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignRequirementResponseDTO getRequirements(String envelopeId) {
        try {
            return clickSignClient.getRequirements(envelopeId);
        } catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao consultar requisitos do envelope no ClickSign.");
        }
    }

    @Override
    public SignatureClickSignRequirementResponseDTO createRequirements(String envelopeId, ClickSignCreateRequestQualifierAttributesDTO request) {
        try {
            return clickSignClient.createRequirements(envelopeId, request);
        }catch (FeignException ex) {
            throw mapFeignException(ex, "Falha ao criar requisitos do requerimento no ClickSign.");
        }
    }

    private RuntimeException mapFeignException(FeignException ex, String fallbackMessage) {
        if (ex.status() == 404) {
            return new ResourceNotFoundException(extractMessage(ex, "Recurso não encontrado no ClickSign."));
        }

        return new ClickSignIntegrationException(extractMessage(ex, fallbackMessage), ex);
    }

    private String extractMessage(FeignException ex, String fallbackMessage) {
        String responseBody = ex.contentUTF8();
        if (responseBody != null && !responseBody.isBlank()) {
            return responseBody;
        }
        if (ex.getMessage() != null && !ex.getMessage().isBlank()) {
            return ex.getMessage();
        }
        return fallbackMessage;
    }

    public SignatureClickSignResponseDTO createEnvelopeFallback(ClickSignCreateEnvelopeRequestDTO createEnvelopeRequestDTO) {
        return ClickSignResponseFactory.failed();

    }
}
