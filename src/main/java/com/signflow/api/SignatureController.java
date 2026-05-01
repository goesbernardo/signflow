package com.signflow.api;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.application.EnvelopeService;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.exception.domain.ErrorResponse;
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

    private final EnvelopeService envelopeService;

    @GetMapping
    @Operation(summary = "Listar envelopes", description = "Retorna uma lista paginada de envelopes vinculados ao usuário autenticado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista recuperada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado")
    })
    public ResponseEntity<Page<Envelope>> listEnvelopes(@Parameter(description = "Filtrar por status") @RequestParam(required = false) Status status, @PageableDefault(size = 10, sort = "created") Pageable pageable) {
        Page<Envelope> response = envelopeService.listEnvelopes(status, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @RateLimiter(name = "userRateLimiter")
    @Operation(summary = "Criar envelope", description = "Cria um novo envelope de assinatura no provedor especificado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Envelope criado com sucesso",
                    content = @Content(schema = @Schema(implementation = Envelope.class), examples = @ExampleObject(value = "{ \"externalId\": \"7db6f70a-0683-4a6c-9a4d-0453715c9298\", \"name\": \"Contrato de Aluguel\", \"status\": \"ACTIVE\", \"created\": \"2024-05-01T14:18:00Z\" }"))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> createEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"name\": \"Contrato de Prestação de Serviços\" }"))) @RequestBody @Valid CreateEnvelopeCommand command) {
        Envelope response = envelopeService.createEnvelope(command, provider);
        log.info("Envelope criado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{externalId}")
    @Operation(summary = "Busca dados do envelope", description = "Busca dados detalhados de um envelope no provedor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envelope recuperado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> getEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        Envelope response = envelopeService.getEnvelope(externalId, provider);
        log.info("Envelope encontrado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PatchMapping("/{externalId}")
    @Operation(summary = "Editar envelope", description = "Atualiza o nome de um envelope existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envelope atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Envelope> updateEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"name\": \"Contrato Atualizado v2\" }")))
            @RequestBody @Valid UpdateEnvelopeCommand command) {
        Envelope response = envelopeService.updateEnvelope(externalId, command, provider);
        log.info("Envelope atualizado com sucesso: {}", response);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{externalId}/signers")
    @RateLimiter(name = "userRateLimiter")
    @Operation(summary = "Adicionar signatário", description = "Adiciona um novo signatário ao envelope para futura assinatura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Signatário adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Signer> addSigner(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"name\": \"João Silva\", \"email\": \"joao.silva@email.com\", \"documentation\": \"123.456.789-00\", \"hasDocumentation\": true, \"delivery\": \"email\" }")))
            @RequestBody @Valid AddSignerCommand command) {
        Signer response = envelopeService.addSigner(externalId, command, provider);
        log.info("assinante adicionado  {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{externalId}/documents")
    @RateLimiter(name = "userRateLimiter")
    @Operation(summary = "Adicionar documento", description = "Adiciona um novo documento (PDF em Base64) ao envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento adicionado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Document> addDocument(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"filename\": \"contrato.pdf\", \"content_base64\": \"JVBERi0xLjQKJ...\", \"email\": \"admin@signflow.com\" }")))
            @RequestBody @Valid AddDocumentCommand command) {
        Document response = envelopeService.addDocument(externalId, command, provider);
        log.info("documento adicionado  {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{externalId}/requirements")
    @Operation(summary = "Adicionar requisito", description = "Vincula um signatário a um documento específico dentro do envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Requisito adicionado com sucesso", content = @Content(schema = @Schema(implementation = Requirement.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Requirement> addRequirement(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(examples = @ExampleObject(value = "{ \"documentId\": \"doc-123\", \"signerId\": \"signer-456\" }")))
            @RequestBody @Valid AddRequirementCommand command) {
        Requirement response = envelopeService.addRequirement(externalId, command, provider);
        log.info("requisito adicionado {}: {}", provider, externalId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{externalId}/activate")
    @Operation(summary = "Ativar envelope", description = "Finaliza a edição do envelope e dispara as notificações para assinatura.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Envelope ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "502", description = "Falha de integração com o provedor")
    })
    public ResponseEntity<Void> activateEnvelope(@Parameter(description = "Provedor de assinatura", example = "CLICKSIGN") @RequestHeader("provider") ProviderSignature provider, @PathVariable String externalId) {
        envelopeService.activateEnvelope(externalId, provider);
        log.info("envelope ativado {}: {}", provider,externalId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{externalId}/timeline")
    @Operation(summary = "Timeline do envelope", description = "Retorna a trilha de auditoria completa de eventos do envelope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timeline recuperada com sucesso", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{ \"previousStatus\": \"PROCESSING\", \"newStatus\": \"ACTIVE\", \"source\": \"SYSTEM\", \"occurredAt\": \"2024-05-01T14:18:00\" }]"))),
            @ApiResponse(responseCode = "401", description = "Não autorizado"),
            @ApiResponse(responseCode = "404", description = "Envelope não encontrado")
    })
    public ResponseEntity<List<EnvelopeTimelineResponse>> getTimeline(@PathVariable String externalId) {
        List<EnvelopeTimelineResponse> response = envelopeService.getTimeline(externalId);
        log.info("Timeline recuperada para o envelope {}: {} eventos", externalId, response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/{provider}")
    @Operation(hidden = true)
    public ResponseEntity<Void> receiveWebhook(@PathVariable String provider, @RequestBody(required = false) String payload) {
        log.info("Webhook recebido do provedor {}: {}", provider, payload);
        return ResponseEntity.ok().build();
    }
}
