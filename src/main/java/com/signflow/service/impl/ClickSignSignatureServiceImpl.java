package com.signflow.service.impl;

import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;
import com.signflow.exception.ClickSignException;
import com.signflow.service.ClickSignSignatureService;
import com.signflow.strategy.SignatureProviderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickSignSignatureServiceImpl implements ClickSignSignatureService {

    @Autowired
    private SignatureProviderStrategy signatureProviderStrategy;

    @Override
    public SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO request) {
        if (request == null) {
            throw new ClickSignException("Request de criação de envelope não pode ser nulo no service");
        }
        log.info("Processando criação de envelope para: {}", request.getData().getAttributes().getName());
        SignatureClickSignResponseDTO response = signatureProviderStrategy.createEnvelope(request);
        return response;
    }

    @Override
    public SignatureClickSignResponseDTO sendDocument(String envelopeId,ClickSignSendDocumentsRequestDTO request) {
        log.info("Processando envio de documento para: {}", request.getData().getAttributes().getName());
        SignatureClickSignResponseDTO response = signatureProviderStrategy.sendDocument(envelopeId,request);
        return response;
    }
}
