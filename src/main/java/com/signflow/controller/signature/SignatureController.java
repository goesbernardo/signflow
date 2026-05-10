package com.signflow.controller.signature;

import com.signflow.dto.domain.DocumentRequestDTO;
import com.signflow.dto.domain.EnvelopeRequestDTO;
import com.signflow.dto.domain.SignatureResponseDTO;
import com.signflow.dto.domain.SignerRequestDTO;
import com.signflow.exception.domain.ErrorResponse;
import com.signflow.exception.domain.InvalidRequestException;
import com.signflow.service.SignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/signatures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Signature", description = "APIs de integracao com provedores de assinatura")
public class SignatureController {

    private final SignatureService signatureService;

    @GetMapping("/webhook/{provider}")
    public ResponseEntity<Void> receiveWebhook(@PathVariable String provider, @RequestBody(required = false) String payload) {
        log.info("Webhook recebido do provedor {}: {}", provider, payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envelopes")
    @Operation(summary = "Criar envelope", description = "Cria um novo envelope de assinatura.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Envelope criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureResponseDTO> createEnvelope(@RequestBody @Valid EnvelopeRequestDTO body) {
        SignatureResponseDTO response = signatureService.createEnvelope(body);
        log.info("Envelope criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "/envelopes/{envelopeId}")
    @Operation(summary = "atualiza envelope", description = "atualiza um envelope.")
    public ResponseEntity<SignatureResponseDTO> updateEnvelope(@PathVariable String envelopeId, @RequestBody EnvelopeRequestDTO body) {
        SignatureResponseDTO response = signatureService.updateEnvelope(envelopeId, body);
        log.info("Envelope atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/envelopes/{envelopeId}")
    @Operation(summary = "busca dados do envelope", description = "busca dados vinculados a um envelope.")
    public ResponseEntity<SignatureResponseDTO> getEnvelopeById(@PathVariable String envelopeId){
        SignatureResponseDTO responseDTO = signatureService.getEnvelopeById(envelopeId);
        log.info("Envelope encontrado com sucesso: {}", responseDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }


    @GetMapping("/envelopes/{envelopeId}/signers")
    @Operation(summary = "Listar signatarios do envelope", description = "Retorna os signatarios vinculados a um envelope.")
    public ResponseEntity<SignatureResponseDTO> getEnvelope(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar signatários.");
        }
        SignatureResponseDTO response = signatureService.getEnvelope(envelopeId);
        log.info("Signatarios encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/envelopes/{envelopeId}/signers")
    @Operation(summary = "Criar assinante", description = "Cria um novo assinante para um envelope.")
    public ResponseEntity<SignatureResponseDTO> createSigner(@PathVariable String envelopeId, @RequestBody @Valid SignerRequestDTO request) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar signatário.");
        }
        SignatureResponseDTO response = signatureService.createSigner(envelopeId,request);
        log.info("Signatario criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/envelopes/{envelopeId}/signers/{signerId}")
    @Operation(summary = "Consultar assinante", description = "Consulta os dados de um assinante especifico.")
    public ResponseEntity<SignatureResponseDTO> getSigner(@PathVariable String envelopeId, @PathVariable String signerId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar um signatário.");
        }
        SignatureResponseDTO response = signatureService.getSigner(envelopeId,signerId);
        log.info("Signatario encontrado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/envelopes/{envelopeId}/documents")
    @Operation(summary = "Listar documentos do envelope", description = "Retorna os documentos de um envelope.")
    public ResponseEntity<SignatureResponseDTO> getDocuments(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para listar documentos.");
        }
        SignatureResponseDTO response = signatureService.getDocuments(envelopeId);
        log.info("Documentos encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/envelopes/{envelopeId}/documents")
    @Operation(summary = "Criar documento por upload", description = "Adiciona um documento ao envelope a partir do conteúdo em base64.")
    public ResponseEntity<SignatureResponseDTO> createDocument(@PathVariable String envelopeId, @RequestBody @Valid DocumentRequestDTO request) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar documento.");
        }
        SignatureResponseDTO response = signatureService.createDocument(envelopeId, request);
        log.info("Documento criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/envelopes/{envelopeId}/documents/{id}")
    @Operation(summary = "Atualizar documento do envelope", description = "Atualiza um documento especifico de um envelope.")
    public ResponseEntity<SignatureResponseDTO> updateDocuments(@PathVariable String envelopeId, @PathVariable String id) {

        SignatureResponseDTO response = signatureService.updateDocuments(envelopeId, id);
        log.info("Documento atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/envelopes/{envelopeId}/requirements")
    @Operation(summary = "Consultar requisitos do requerimento", description = "Retorna os requisitos configurados para um requerimento.")
    public ResponseEntity<SignatureResponseDTO> getRequirements(@PathVariable String envelopeId) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar requisitos.");
        }

        SignatureResponseDTO response = signatureService.getRequirements(envelopeId);
        log.info("Requisitos encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/envelopes/{envelopeId}/requirements")
    @Operation(summary = "criar requisitos do requerimento", description = "cria requisitos de um requerimento")
    public ResponseEntity<SignatureResponseDTO> createRequirements(@PathVariable String envelopeId, @RequestBody Object request) {
        SignatureResponseDTO response = signatureService.createRequirements(envelopeId, request);
        log.info("Requisitos criados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/envelopes/{envelopeId}/activate")
    @Operation(summary = "criar ativação de um envelope", description = "cria ativação de um envelope")
    public ResponseEntity<SignatureResponseDTO> activateEnvelope(@PathVariable String envelopeId, @RequestBody Object request) {
        SignatureResponseDTO response = signatureService.activateEnvelope(envelopeId, request);
        log.info("envelope ativado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
