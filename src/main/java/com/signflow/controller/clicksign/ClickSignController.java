package com.signflow.controller.clicksign;

import com.signflow.dto.clicksign.request.*;
import com.signflow.dto.clicksign.response.SignatureClickSignResponseDTO;
import com.signflow.exception.clicksign.ErrorResponse;
import com.signflow.exception.clicksign.InvalidRequestException;
import com.signflow.service.ClicksignService;
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
@RequestMapping("api/v1/clicksign")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ClickSign", description = "APIs de integracao com o provedor ClickSign")
public class ClickSignController {

    private final ClicksignService clickSignSignatureService;

    @GetMapping("/webhook/clicksign")
    public ResponseEntity<Void> receiveWebhook(@RequestBody(required = false) String payload) {
        log.info("Webhook recebido da Clicksign: {}", payload);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/create-envelope")
    @Operation(summary = "Criar envelope", description = "Cria um novo envelope no ClickSign.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Envelope criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> createEnvelope(@RequestBody @Valid ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body) {
        SignatureClickSignResponseDTO response = clickSignSignatureService.createEnvelope(body);
        log.info("Envelope criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping(value = "envelopes/{envelopeId}")
    @Operation(summary = "atualiza  envelope", description = "atualiza um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> updateEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignEnvelopeAttributesDTO, Void>> body) {
        SignatureClickSignResponseDTO response = clickSignSignatureService.updateEnvelope(envelopeId, body);
        log.info("Envelope atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "envelopes/{envelopeId}")
    @Operation(summary = "busca dados do envelope", description = "busca dados vinculados a um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> getEnvelopeById(@PathVariable String envelopeId){
        SignatureClickSignResponseDTO responseDTO = clickSignSignatureService.getEnvelopeById(envelopeId);
        log.info("Envelope encontrado com sucesso: {}", responseDTO);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }


    @GetMapping("/envelopes/{envelopeId}/signers")
    @Operation(summary = "Listar signatarios do envelope", description = "Retorna os signatarios vinculados a um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatarios retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> getEnvelope(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar signatários.");
        }
        SignatureClickSignResponseDTO response = clickSignSignatureService.getEnvelope(envelopeId);
        log.info("Signatarios encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("envelopes/{envelopeId}/create-signer")
    @Operation(summary = "Criar assinante", description = "Cria um novo assinante para um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Signatario criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> createSigner(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateSignAttributesDTO,Void>> request) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar signatário.");
        }
        SignatureClickSignResponseDTO response = clickSignSignatureService.createSigner(envelopeId,request);
        log.info("Signatario criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/signers/{signerId}")
    @Operation(summary = "Consultar assinante", description = "Consulta os dados de um assinante especifico.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Signatario retornado com sucesso"), @ApiResponse(responseCode = "400", description = "Parametros invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Signatario nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> getSigner(@PathVariable String envelopeId, @PathVariable String signerId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar um signatário.");
        }
        SignatureClickSignResponseDTO response = clickSignSignatureService.getSigner(envelopeId,signerId);
        log.info("Signatario encontrado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/documents")
    @Operation(summary = "Listar documentos do envelope", description = "Retorna os documentos de um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Documentos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),@ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> getDocuments(@PathVariable String envelopeId) {

        if(envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para listar documentos.");
        }
        SignatureClickSignResponseDTO response = clickSignSignatureService.getDocuments(envelopeId);
        log.info("Documentos encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("envelopes/{envelopeId}/documents")
    @Operation(summary = "Criar documento por upload", description = "Adiciona um documento ao envelope a partir do conteúdo em base64.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Documento criado com sucesso"), @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> createDocument(@PathVariable String envelopeId, @RequestBody @Valid ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignCreateDocumentAttributesDTO,Void>> request) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para criar documento.");
        }
        SignatureClickSignResponseDTO response = clickSignSignatureService.createDocument(envelopeId, request);
        log.info("Documento criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("envelopes/{envelopeId}/documents/{id}")
    @Operation(summary = "Atualizar documento do envelope", description = "Atualiza um documento especifico de um envelope.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Documento atualizado com sucesso"), @ApiResponse(responseCode = "400", description = "Parametros invalidos", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Documento ou envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> updateDocuments(@PathVariable String envelopeId, @PathVariable String id) {

        SignatureClickSignResponseDTO response = clickSignSignatureService.updateDocuments(envelopeId, id);
        log.info("Documento atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("envelopes/{envelopeId}/requirements")
    @Operation(summary = "Consultar requisitos do requerimento", description = "Retorna os requisitos configurados para um requerimento.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Requisitos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> getRequirements(@PathVariable String envelopeId) {

        if (envelopeId == null || envelopeId.isBlank()) {
            throw new InvalidRequestException("O envelopeId é obrigatório para consultar requisitos.");
        }

        SignatureClickSignResponseDTO response = clickSignSignatureService.getRequirements(envelopeId);
        log.info("Requisitos encontrados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("envelopes/{envelopeId}/requirements")
    @Operation(summary = "criar requisitos do requerimento", description = "cria requisitos de um requerimento na clicksign")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Requisitos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> createRequirements(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClickSignRequirementsAttributesDTO,ClickSignRequirementsRelationshipDTO>> request) {
        SignatureClickSignResponseDTO response = clickSignSignatureService.createRequirements(envelopeId, request);
        log.info("Requisitos criados com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/envelopres/{envelopeId}")
    @Operation(summary = "criar ativação de um envelope", description = "cria ativação de um envelope na clicksign")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Requisitos retornados com sucesso"), @ApiResponse(responseCode = "400", description = "EnvelopeId invalido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "404", description = "Envelope nao encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))), @ApiResponse(responseCode = "502", description = "Falha de integracao com ClickSign", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
    public ResponseEntity<SignatureClickSignResponseDTO> activateEnvelope(@PathVariable String envelopeId, @RequestBody ClickSignRequestApiDTO<ClickSignRequestApiDataDTO<ClicksignActivateAttributesDTO,Void>> request) {
        SignatureClickSignResponseDTO response = clickSignSignatureService.activateEnvelope(envelopeId, request);
        log.info("envelope ativado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


}
