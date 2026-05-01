package com.signflow.api;

import com.signflow.application.EnvelopeService;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.exception.domain.ErrorResponse;
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

    private final EnvelopeService envelopeService;

    @PostMapping
    @Operation(summary = "Criar envelope", description = "Cria um novo envelope de assinatura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envelope criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> createEnvelope(@RequestHeader("provider") ProviderSignature provider, @RequestBody @Valid CreateEnvelopeCommand command) {
        Envelope response = envelopeService.createEnvelope(command, provider);
        log.info("Envelope criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Busca dados do envelope", description = "Busca dados vinculados a um envelope no provedor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envelope recuperado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> getEnvelope(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        Envelope response = envelopeService.getEnvelope(externalId, provider);
        log.info("Envelope encontrado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{externalId}")
    @Operation(summary = "Editar envelope", description = "Edita as configurações de um envelope existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envelope atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> updateEnvelope(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @RequestBody @Valid UpdateEnvelopeCommand command) {
        Envelope response = envelopeService.updateEnvelope(externalId, command, provider);
        log.info("Envelope atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{externalId}/signers")
    @Operation(summary = "Adicionar signatário", description = "Adiciona um novo signatário ao envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Signatário adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Signer> addSigner(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @RequestBody @Valid AddSignerCommand command) {
        Signer response = envelopeService.addSigner(externalId, command, provider);
        log.info("assinante adicionado  {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{externalId}/documents")
    @Operation(summary = "Adicionar documento", description = "Adiciona um novo documento ao envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Document> addDocument(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @RequestBody @Valid AddDocumentCommand command) {
        Document response = envelopeService.addDocument(externalId, command, provider);
        log.info("documento adicionado  {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{externalId}/requirements")
    @Operation(summary = "Adicionar requisito", description = "Vincula um signatário a um documento no envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Requisito adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> addRequirement(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @RequestBody @Valid AddRequirementCommand command) {
        envelopeService.addRequirement(externalId, command, provider);
        log.info("requisito adicionado {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{externalId}/activate")
    @Operation(summary = "Ativar envelope", description = "Ativa o envelope para que os signatários possam assinar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envelope ativado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisicao invalida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integracao com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> activateEnvelope(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        envelopeService.activateEnvelope(externalId, provider);
        log.info("envelope ativado {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/webhook/{provider}")
    public ResponseEntity<Void> receiveWebhook(@PathVariable String provider, @RequestBody(required = false) String payload) {
        log.info("Webhook recebido do provedor {}: {}", provider, payload);
        return ResponseEntity.ok().build();
    }
}
