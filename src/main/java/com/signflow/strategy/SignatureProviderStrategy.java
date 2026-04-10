package com.signflow.strategy;

import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;

public interface SignatureProviderStrategy {

    SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO signatureClickSignRequestDTO);

    SignatureClickSignResponseDTO sendDocument(String envelopeId , ClickSignSendDocumentsRequestDTO signatureClickSignRequestDTO);

}
