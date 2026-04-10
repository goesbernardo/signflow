package com.signflow.controller;

import com.signflow.dto.clicksign.ClickSignCreateEnvelopeRequestDTO;
import com.signflow.dto.clicksign.ClickSignSendDocumentsRequestDTO;
import com.signflow.dto.clicksign.SignatureClickSignResponseDTO;
import com.signflow.exception.ClickSignException;
import com.signflow.service.ClickSignSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/signatures/clicksign")
@RequiredArgsConstructor
@Slf4j
public class SignatureController {

    @Autowired
    private ClickSignSignatureService clickSignSignatureService;

    @PostMapping("/create-envelope")
    public ResponseEntity<SignatureClickSignResponseDTO> createEnvelope(@RequestBody ClickSignCreateEnvelopeRequestDTO request) {
        if (request == null) {
            throw new ClickSignException("O corpo da requisição não pode ser nulo");
        }
        log.info("Recebida requisição para criação de envelope na ClickSign: {}", request.getData().getAttributes().getName());
        SignatureClickSignResponseDTO response = clickSignSignatureService.createEnvelope(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{envelope_id}/send-document")
    public ResponseEntity<SignatureClickSignResponseDTO> sendEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignSendDocumentsRequestDTO request) {
        log.info("Recebida requisição para envio de documento na ClickSign: {}", request.getData().getAttributes().getName());
        SignatureClickSignResponseDTO response = clickSignSignatureService.sendDocument(envelopeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
