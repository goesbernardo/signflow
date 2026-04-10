package com.signflow.service;

import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;

public interface ClickSignSignatureService {

    SignatureClickSignResponseDTO createEnvelope(ClickSignCreateEnvelopeRequestDTO request);

    SignatureClickSignResponseDTO sendDocument(String envelopeId,ClickSignSendDocumentsRequestDTO request);

}
