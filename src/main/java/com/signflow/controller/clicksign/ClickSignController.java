package com.signflow.controller.clicksign;

import com.signflow.dto.clicksign.*;
import com.signflow.exception.clicksign.InvalidRequestException;
import com.signflow.exception.clicksign.ErrorResponse;
import com.signflow.service.ClickSignSignatureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/signatures/clicksign")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ClickSign", description = "APIs de integracao com o provedor ClickSign")
public class ClickSignController {

    @Autowired
    private ClickSignSignatureService clickSignSignatureService;

    @PostMapping("/create-envelope")
    @Operation(summary = "Criar envelope", description = "Cria um novo envelope no ClickSign.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Envelope criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> createEnvelope(@RequestBody @Valid ClickSignCreateEnvelopeRequestDTO request) {

        request.setId(UUID.randomUUID());

        SignatureClickSignResponseDTO response = clickSignSignatureService.createEnvelope(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "envelopes/{envelopeId}")
    @Operation(summary = "atualiza  envelope", description = "atualiza um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignUpdateResponseDTO> updateEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignUpdateEnvelopeRequestDTO request) {
        SignatureClickSignUpdateResponseDTO response = clickSignSignatureService.updateEnvelope(envelopeId, request);
        request.setId(UUID.randomUUID());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "envelopes/{envelopeId}")
    @Operation(summary = "busca dados do envelope", description = "busca dados vinculados a um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignGetResponseDTO> getEnvelopeById(@PathVariable String envelopeId){
        SignatureClickSignGetResponseDTO responseDTO = clickSignSignatureService.getEnvelopeById(envelopeId);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }


    @GetMapping("/envelopes/{envelopeId}/signers")
    @Operation(summary = "Listar signatarios do envelope", description = "Retorna os signatarios vinculados a um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignListResponseSignersDTO> getEnvelope(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar signatários.");
        }
        SignatureClickSignListResponseSignersDTO response = clickSignSignatureService.getEnvelope(envelopeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("envelopes/{envelopeId}/create-signer")
    @Operation(summary = "Criar signatario", description = "Cria um novo signatario para um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Signatario criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignSignerResponseDTO> createSigner(@PathVariable String envelopeId, @RequestBody ClickSignCreateSignerRequestDTO request) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar signatário.");
        }
        SignatureClickSignSignerResponseDTO response = clickSignSignatureService.createSigner(envelopeId,request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/signers/{signerId}")
    @Operation(summary = "Consultar signatario", description = "Consulta os dados de um signatario especifico.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatario retornado com sucesso"), @ApiResponse(responseCode = "400", description = "Parametros invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Signatario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignSignerResponseDTO> getSigner(@PathVariable String envelopeId,@PathVariable String signerId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar um signatário.");
        }
        SignatureClickSignSignerResponseDTO response = clickSignSignatureService.getSigner(envelopeId,signerId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/documents")
    @Operation(summary = "Listar documentos do envelope", description = "Retorna os documentos de um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Documentos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),@ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignDocumentListResponseDTO> getDocuments(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para listar documentos.");
        }
        SignatureClickSignDocumentListResponseDTO response = clickSignSignatureService.getDocuments(envelopeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("envelopes/{envelopeId}/documents")
    @Operation(summary = "Criar documento por upload", description = "Adiciona um documento ao envelope a partir do conteúdo em base64.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Documento criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignDocumentResponseDTO> createDocument(@PathVariable String envelopeId, @RequestBody @Valid ClickSignCreateDocumentDTO request) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar documento.");
        }
        SignatureClickSignDocumentResponseDTO response = clickSignSignatureService.createDocument(envelopeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("envelopes/{envelopeId}/documents/{id}")
    @Operation(summary = "Atualizar documento do envelope", description = "Atualiza um documento especifico de um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso"), @ApiResponse(responseCode = "400", description = "Parametros invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Documento ou envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignDocumentListResponseDTO> updateDocuments(@PathVariable String envelopeId, @PathVariable String id) {

        SignatureClickSignDocumentListResponseDTO response = clickSignSignatureService.updateDocuments(envelopeId, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/requirements")
    @Operation(summary = "Consultar requisitos do envelope", description = "Retorna os requisitos configurados para um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Requisitos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignRequirementResponseDTO> getRequirements(@PathVariable String envelopeId) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar requisitos.");
        }

        SignatureClickSignRequirementResponseDTO response = clickSignSignatureService.getRequirements(envelopeId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
