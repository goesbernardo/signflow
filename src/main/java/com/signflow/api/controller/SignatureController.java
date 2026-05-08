package com.signflow.api.controller;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.application.port.in.SignatureService;
import com.signflow.domain.command.CreateFullEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.command.UpdateDocumentCommand;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/signatures")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Assinaturas", description = "APIs de integração com provedores de assinatura")
@SecurityRequirement(name = "Bearer Authentication")
public class SignatureController {

    private final SignatureService signatureService;

    @GetMapping
    @Operation(summary = "Listar envelopes", description = "Retorna uma lista paginada de envelopes vinculados ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<Envelope>> listEnvelopes(@Parameter(description = "Filtrar por status") @RequestParam(required = false) Status status,
                                                        @Parameter(description = "Incluir detalhes dos signatários") @RequestParam(required = false, defaultValue = "false") boolean includeSigners,
                                                        @PageableDefault(size = 10, sort = "created") Pageable pageable) {
        Page<Envelope> response = signatureService.listEnvelopes(status, pageable, includeSigners);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Busca dados do envelope", description = "Busca dados detalhados de um envelope no provedor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envelope recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor", content = @Content(schema = @Schema(implementation = org.springframework.web.ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> getEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider,
                                               @Parameter(description = "Incluir detalhes dos signatários") @RequestParam(required = false, defaultValue = "false") boolean includeSigners,
                                               @PathVariable String externalId) {
        Envelope response = signatureService.getEnvelope(externalId, provider, includeSigners);
        log.info("Envelope encontrado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{externalId}")
    @Operation(summary = "Editar envelope", description = "Atualiza o nome de um envelope existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envelope atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor", content = @Content(schema = @Schema(implementation = org.springframework.web.ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> updateEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId,
                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"name\": \"Contrato Atualizado v2\" }")))
                                                   @RequestBody @Valid UpdateEnvelopeCommand command) {
        Envelope response = signatureService.updateEnvelope(externalId, command, provider);
        log.info("Envelope atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{externalId}/timeline")
    @Operation(summary = "Timeline do envelope", description = "Retorna a trilha de auditoria completa de eventos do envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timeline recuperada com sucesso", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{ \"previousStatus\": \"PROCESSING\", \"newStatus\": \"ACTIVE\", \"source\": \"SYSTEM\", \"occurredAt\": \"2024-05-01T14:18:00\" }]"))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado")
    })
    public ResponseEntity<List<EnvelopeTimelineResponse>> getTimeline(@PathVariable String externalId) {List<EnvelopeTimelineResponse> response = signatureService.getTimeline(externalId);
        log.info("Timeline recuperada para o envelope {}: {} eventos", externalId, response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-activate-envelope")
    @RateLimiter(name = "userRateLimiter")
    @Operation(summary = "Criar envelope completo", description = "Cria um envelope, documentos, signatários e requisitos em uma única chamada. Opcionalmente ativa o envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envelope completo criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Envelope> createFullEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider,
                                                       @RequestBody @Valid CreateFullEnvelopeCommand command) {
        Envelope response = signatureService.createFullEnvelope(command, provider);
        log.info("Envelope completo criado com sucesso: {}", response != null ? response.getExternalId() : "null");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{externalId}/cancel")
    @Operation(summary = "Cancelar envelope", description = "Cancela um envelope no provedor e atualiza o status local para CANCELED. Somente envelopes ACTIVE ou DRAFT podem ser cancelados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Envelope cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Status do envelope não permite cancelamento"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Void> cancelEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider,
                                               @PathVariable String externalId) {
        signatureService.cancelEnvelope(externalId, provider);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{externalId}/activate")
    @Operation(summary = "Ativar envelope", description = "Ativa um envelope em rascunho (DRAFT) no provedor e dispara as notificações. Retorna 409 se o envelope já estiver ativo ou cancelado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Envelope ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "409", description = "O envelope não está em status permitido para ativação"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Void> activateEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider,
                                                 @PathVariable String externalId) {
        try {
            signatureService.activateEnvelope(externalId, provider);
            return ResponseEntity.noContent().build();
        } catch (com.signflow.domain.exception.DomainException e) {
            if (e.getMessage().contains("DRAFT")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    @GetMapping("/{externalId}/documents")
    @Operation(summary = "Listar documentos do envelope", description = "Retorna os documentos associados a um envelope.")
    public ResponseEntity<List<Document>> getDocuments(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        return ResponseEntity.ok(signatureService.getDocuments(externalId, provider));
    }

    @GetMapping("/documents/{documentId}")
    @Operation(summary = "Visualizar documento", description = "Busca detalhes de um documento específico.")
    public ResponseEntity<Document> getDocument(@RequestHeader("provider") ProviderSignature provider, @PathVariable String documentId) {
        return ResponseEntity.ok(signatureService.getDocument(documentId, provider));
    }

    @PatchMapping("/documents/{documentId}")
    @Operation(summary = "Editar documento", description = "Atualiza os dados de um documento (ex: nome do arquivo).")
    public ResponseEntity<Document> updateDocument(@RequestHeader("provider") ProviderSignature provider, @PathVariable String documentId, @RequestBody @Valid UpdateDocumentCommand command) {
        return ResponseEntity.ok(signatureService.updateDocument(documentId, command, provider));
    }

    @DeleteMapping("/documents/{documentId}")
    @Operation(summary = "Excluir documento", description = "Remove um documento permanentemente.")
    public ResponseEntity<Void> deleteDocument(@RequestHeader("provider") ProviderSignature provider, @PathVariable String documentId) {
        signatureService.deleteDocument(documentId, provider);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{externalId}/signers")
    @Operation(summary = "Listar signatários do envelope", description = "Retorna os signatários associados a um envelope.")
    public ResponseEntity<List<Signer>> getSigners(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        return ResponseEntity.ok(signatureService.getSigners(externalId, provider));
    }

    @GetMapping("/{externalId}/signers/{signerId}")
    @Operation(summary = "Visualizar signatário", description = "Busca detalhes de um signatário específico de um envelope.")
    public ResponseEntity<Signer> getSigner(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @PathVariable String signerId) {
        return ResponseEntity.ok(signatureService.getSigner(externalId, signerId, provider));
    }

    @DeleteMapping("/{externalId}/signers/{signerId}")
    @Operation(summary = "Excluir signatário", description = "Remove um signatário permanentemente de um envelope.")
    public ResponseEntity<Void> deleteSigner(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId, @PathVariable String signerId) {
        signatureService.deleteSigner(externalId, signerId, provider);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{externalId}/requirements")
    @Operation(summary = "Listar requisitos do envelope", description = "Retorna os requisitos associados a um envelope.")
    public ResponseEntity<List<Requirement>> getRequirements(@RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        return ResponseEntity.ok(signatureService.getRequirements(externalId, provider));
    }

    @GetMapping("/requirements/{requirementId}")
    @Operation(summary = "Visualizar requisito", description = "Busca detalhes de um requisito específico.")
    public ResponseEntity<Requirement> getRequirement(@RequestHeader("provider") ProviderSignature provider, @PathVariable String requirementId) {
        return ResponseEntity.ok(signatureService.getRequirement(requirementId, provider));
    }

    @DeleteMapping("/requirements/{requirementId}")
    @Operation(summary = "Excluir requisito", description = "Remove um requisito permanentemente.")
    public ResponseEntity<Void> deleteRequirement(@RequestHeader("provider") ProviderSignature provider, @PathVariable String requirementId) {
        signatureService.deleteRequirement(requirementId, provider);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{envelopeId}/signers/{signerId}/remind")
    @Operation(summary = "Lembrar signatário", description = "Envia um lembrete manual para o signatário assinar o documento.")
    public ResponseEntity<Void> remindSigner(
            @RequestHeader("provider") ProviderSignature provider,
            @PathVariable String envelopeId,
            @PathVariable String signerId) {
        log.info("Recebida requisição de lembrete para o signatário {} do envelope {} no provedor {}", signerId, envelopeId, provider);
        signatureService.remindSigner(envelopeId, signerId, provider);
        return ResponseEntity.noContent().build();
    }
}
