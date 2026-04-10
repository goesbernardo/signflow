package com.signflow.strategy.impl;

import com.signflow.client.clicksign.ClickSignClient;
import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;
import com.signflow.strategy.SignatureProviderStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickSignSignatureProvider implements SignatureProviderStrategy {

    private final ClickSignClient clickSignClient;


    @Override
    public SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO signatureClickSignRequestDTO) {
        log.info("Creating envelope in ClickSign: {}", signatureClickSignRequestDTO.getData().getAttributes().getName());
        return clickSignClient.createEnvelope(signatureClickSignRequestDTO);
    }

    @Override
    public SignatureClickSignResponseDTO sendDocument(String envelopeId,ClickSignSendDocumentsRequestDTO signatureClickSignRequestDTO) {
        log.info("sending document in ClickSign: {}", signatureClickSignRequestDTO.getData().getAttributes().getName());
        return clickSignClient.sendDocuments(envelopeId,signatureClickSignRequestDTO);
    }
}
