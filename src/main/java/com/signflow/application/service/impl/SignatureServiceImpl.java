package com.signflow.application.service.impl;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.api.dto.OutboundWebhookDeliveryResponse;
import com.signflow.application.port.in.SignatureService;
import com.signflow.application.port.out.ESignatureGateway;
import com.signflow.application.service.AuditLogService;
import com.signflow.application.webhook.NormalizedWebhookEvent;
import com.signflow.config.KafkaConfig;
import com.signflow.domain.command.*;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.*;
import com.signflow.infrastructure.gateway.SignatureGatewayRegistry;
import com.signflow.infrastructure.persistence.entity.*;
import com.signflow.infrastructure.persistence.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignatureServiceImpl implements SignatureService {

    private final SignatureGatewayRegistry registry;
    private final EnvelopeRepository repository;
    private final SignerRepository signerRepository;
    private final DocumentRepository documentRepository;
    private final RequirementRepository requirementRepository;
    private final EnvelopeEventRepository eventRepository;
    private final NotifierRepository notifierRepository;
    private final OutboundWebhookDeliveryRepository outboundWebhookDeliveryRepository;
    private final com.signflow.application.service.SmartRoutingService smartRoutingService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final HttpServletRequest request;

    // ── createEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("CREATE_ENVELOPE", "ENVELOPE", null, "Criação de envelope: " + cmd.name());
        ProviderSignature resolvedProvider = provider != null ? provider : smartRoutingService.route(userId, cmd);
        log.info("Iniciando criação de envelope para o provedor {}", resolvedProvider);

        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setStatus(Status.PROCESSING);
        entity.setProvider(resolvedProvider);
        entity.setName(cmd.name());
        entity.setCallbackUrl(cmd.callbackUrl());
        entity.setCreated(LocalDateTime.now());

        entity.setUserId(userId);

        entity = repository.save(entity);
        saveEvent(entity, null, Status.PROCESSING);

        try {
            ESignatureGateway gateway = registry.get(resolvedProvider);
            Envelope envelope = gateway.createEnvelope(cmd);

            updateStatus(entity, Status.DRAFT);
            entity.setExternalId(envelope.getExternalId());
            repository.save(entity);

            envelope.setId(entity.getId().toString());
            return envelope;
        } catch (Exception e) {
            log.error("Erro ao criar envelope no provedor {}", resolvedProvider, e);
            updateStatus(entity, Status.FAILED);
            throw e;
        }
    }

    // ── updateEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Atualizando envelope {} no provedor {}", externalId, resolvedProvider);
        Envelope envelope = registry.get(resolvedProvider).updateEnvelope(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(entity -> {
            entity.setName(cmd.name());
            repository.save(entity);
        });

        return envelope;
    }

    // ── getEnvelope ───────────────────────────────────────────────────────

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider, boolean includeSigners) {
        String currentUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByExternalId(externalId)
                .map(entity -> {
                    auditLogService.log("GET_ENVELOPE", "ENVELOPE", externalId, "Acesso local ao envelope");
                    log.info("[AUDIT] Usuário {} acessou envelope local {}.", currentUserId, externalId);
                    Envelope envelope = Envelope.builder()
                            .id(entity.getId().toString())
                            .externalId(entity.getExternalId())
                            .name(entity.getName())
                            .status(entity.getStatus())
                            .provider(String.valueOf(entity.getProvider()))
                            .callbackUrl(entity.getCallbackUrl())
                            .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                            .build();

                    if (includeSigners && entity.getSigners() != null) {
                        envelope.setSigners(entity.getSigners().stream()
                                .map(this::mapToSignerModel)
                                .collect(Collectors.toList()));
                    }

                    return envelope;
                })
                .orElseGet(() -> {
                    ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
                    auditLogService.log("GET_ENVELOPE_REMOTE", "ENVELOPE", externalId, "Acesso remoto ao envelope no provedor " + resolvedProvider);
                    log.info("[AUDIT] Usuário {} consultou envelope remoto {} no provedor {}.", currentUserId, externalId, resolvedProvider);
                    return registry.get(resolvedProvider).getEnvelope(externalId);
                });
    }

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider) {
        return getEnvelope(externalId, provider, false);
    }

    // ── activateEnvelope ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void activateEnvelope(String externalId, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Ativando envelope {} no provedor {}", externalId, resolvedProvider);

        EnvelopeEntity entity = repository.findByExternalId(externalId).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Envelope não encontrado: " + externalId));

        if (entity.getStatus() != Status.DRAFT) {
            throw new DomainException(DomainErrorCode.INVALID_ENVELOPE_STATUS, "Somente envelopes em rascunho (DRAFT) podem ser ativados. Status atual: " + entity.getStatus());
        }

        registry.get(resolvedProvider).activateEnvelope(externalId);
        updateStatus(entity, Status.ACTIVE);
        auditLogService.log("ACTIVATE_ENVELOPE", "ENVELOPE", externalId, "Ativação do envelope no provedor " + resolvedProvider);
    }

    // ── cancelEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelEnvelope(String externalId, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Cancelando envelope {} no provedor {}", externalId, resolvedProvider);

        EnvelopeEntity entity = repository.findByExternalId(externalId).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Envelope não encontrado: " + externalId));

        if (entity.getStatus() != Status.ACTIVE && entity.getStatus() != Status.DRAFT) {
            throw new DomainException(DomainErrorCode.INVALID_ENVELOPE_STATUS, "Somente envelopes ACTIVE ou DRAFT podem ser cancelados. Status atual: " + entity.getStatus());
        }

        registry.get(resolvedProvider).cancelEnvelope(externalId);
        updateStatus(entity, Status.CANCELED);
        auditLogService.log("CANCEL_ENVELOPE", "ENVELOPE", externalId, "Cancelamento do envelope no provedor " + resolvedProvider);
    }

    private ProviderSignature resolveProviderForEnvelope(String externalId, ProviderSignature provider) {
        if (provider != null) return provider;
        return repository.findByExternalId(externalId)
                .map(EnvelopeEntity::getProvider).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Provider não informado e envelope não encontrado localmente para o ID: " + externalId));
    }

    // ── listEnvelopes ─────────────────────────────────────────────────────

    @Override
    public Page<Envelope> listEnvelopes(Status status, Pageable pageable, boolean includeSigners) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.log("LIST_ENVELOPES", "ENVELOPE", null, "Listagem de envelopes com status " + status);
        log.info("[AUDIT] Usuário {} listou seus envelopes com status {} (includeSigners={})", userId, status, includeSigners);

        Page<EnvelopeEntity> entities = status != null
                ? repository.findAllByUserIdAndStatus(userId, status, pageable)
                : repository.findAllByUserId(userId, pageable);

        return entities.map(entity -> {
            Envelope envelope = Envelope.builder()
                    .id(entity.getId().toString())
                    .externalId(entity.getExternalId())
                    .name(entity.getName())
                    .status(entity.getStatus())
                    .created(entity.getCreated().atOffset(ZoneOffset.UTC))
                    .build();

            if (includeSigners && entity.getSigners() != null) {
                envelope.setSigners(entity.getSigners().stream()
                        .map(this::mapToSignerModel)
                        .collect(Collectors.toList()));
            }

            return envelope;
        });
    }

    @Override
    public Page<Envelope> listEnvelopes(Status status, Pageable pageable) {
        return listEnvelopes(status, pageable, false);
    }

    // ── getTimeline ───────────────────────────────────────────────────────

    @Override
    public List<EnvelopeTimelineResponse> getTimeline(String externalId) {
        log.info("Buscando timeline para o envelope {}", externalId);
        auditLogService.log("GET_TIMELINE", "ENVELOPE", externalId, "Acesso à timeline do envelope");
        return eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc(externalId)
                .stream()
                .map(event -> EnvelopeTimelineResponse.builder()
                        .previousStatus(event.getPreviousStatus())
                        .newStatus(event.getNewStatus())
                        .source(event.getSource())
                        .providerEvent(event.getProviderEvent())
                        .providerStatus(event.getProviderStatus())
                        .signerExternalId(event.getSigner() != null ? event.getSigner().getExternalId() : null)
                        .occurredAt(event.getOccurredAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ── createFullEnvelope ────────────────────────────────────────────────

    /**
     * Fluxo completo em uma chamada:
     * 1. Criar envelope
     * 2. Adicionar documentos
     * 3. Adicionar signatários
     * 4. Criar requisitos (qualificação + autenticação por signatário × documento)
     * 5. Ativar automaticamente (se autoActivate = true)
     * <p>
     * Melhoria #01 aplicada:
     * O FullRequirementCommand agora usa SignerRole e SignatureAuthMethod (domínio neutro).
     * Os métodos mapToSignerRole() e mapToAuthMethod() foram removidos — o mapeamento
     * para os tipos internos do provider é feito exclusivamente no gateway.
     */
    @Override
    @Transactional
    public Envelope createFullEnvelope(CreateFullEnvelopeCommand cmd, ProviderSignature provider) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        ProviderSignature resolvedProvider = provider != null ? provider : smartRoutingService.route(userId, cmd);
        log.info("Iniciando criação de envelope completo: {} no provedor {}", cmd.name(), resolvedProvider);

        // 1. Criar Envelope
        Envelope envelope = createEnvelope(CreateEnvelopeCommand.builder()
                .name(cmd.name())
                .callbackUrl(cmd.callbackUrl())
                .build(), resolvedProvider);
        String envelopeId = envelope.getExternalId();
        log.info("Envelope criado com ID: {}", envelopeId);

        // 2. Adicionar Documentos
        List<String> documentIds = new ArrayList<>();
        if (cmd.documents() != null) {
            for (AddDocumentCommand docCmd : cmd.documents()) {
                Document doc = addDocument(envelopeId, docCmd, resolvedProvider);
                documentIds.add(doc.getExternalId());
            }
        }
        log.info("Documentos adicionados: {}", documentIds);

        // 3. Adicionar Signatários
        List<String> signerIds = new ArrayList<>();
        if (cmd.signers() != null) {
            List<Signer> signers = addSigners(envelopeId, cmd.signers(), resolvedProvider);
            signers.forEach(s -> signerIds.add(s.getExternalId()));
        }
        log.info("Signatários adicionados: {}", signerIds);

        // 4. Adicionar Requisitos
        if (cmd.requirements() != null && !documentIds.isEmpty() && !signerIds.isEmpty()) {
            for (FullRequirementCommand reqCmd : cmd.requirements()) {

                SignerRole role = reqCmd.role() != null ? reqCmd.role() : SignerRole.SIGN;
                SignatureAuthMethod auth = reqCmd.auth() != null ? reqCmd.auth() : SignatureAuthMethod.EMAIL;

                for (String signerId : signerIds) {
                    for (String documentId : documentIds) {

                        // 4a. Requisito de Qualificação — role
                        addRequirement(envelopeId, AddRequirementCommand.builder()
                                .signerId(signerId)
                                .documentId(documentId)
                                .role(role)
                                .build(), resolvedProvider);

                        // 4b. Requisito de Autenticação — auth
                        addRequirement(envelopeId, AddRequirementCommand.builder()
                                .signerId(signerId)
                                .documentId(documentId)
                                .auth(auth)
                                .build(), resolvedProvider);
                    }
                }
            }
        }
        log.info("Requisitos adicionados");

        // 5. Ativação automática
        if (Boolean.TRUE.equals(cmd.autoActivate())) {
            activateEnvelope(envelopeId, resolvedProvider);
            log.info("Envelope {} ativado automaticamente", envelopeId);
        }

        return getEnvelope(envelopeId, resolvedProvider);
    }

    // ── Documento ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Adicionando documento ao envelope {} no provedor {}", externalId, resolvedProvider);
        Document document = registry.get(resolvedProvider).addDocument(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            DocumentEntity entity = new DocumentEntity();
            entity.setExternalId(document.getExternalId());
            entity.setFilename(cmd.filename());
            entity.setEnvelope(envelope);
            entity.setCreated(LocalDateTime.now());
            documentRepository.save(entity);
        });

        return document;
    }

    @Override
    public List<Document> getDocuments(String externalId, ProviderSignature provider) {
        log.info("Listando documentos do envelope {} no banco local", externalId);
        return documentRepository.findAllByEnvelopeExternalId(externalId)
                .stream()
                .map(entity -> Document.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Document getDocument(String documentId, ProviderSignature provider) {
        return documentRepository.findByExternalId(documentId)
                .map(entity -> Document.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Documento não encontrado: " + documentId));
    }

    @Override
    public Document updateDocument(String documentId, UpdateDocumentCommand cmd, ProviderSignature provider) {
        // Operação ainda não suportada no gateway (Provider)
        DocumentEntity entity = documentRepository.findByExternalId(documentId).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Documento não encontrado: " + documentId));

        if (cmd.filename() != null) entity.setFilename(cmd.filename());
        documentRepository.save(entity);

        return Document.builder()
                .externalId(entity.getExternalId())
                .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    @Override
    @Transactional
    public void deleteDocument(String documentId, ProviderSignature provider) {
        // Operação ainda não suportada no gateway (Provider)
        documentRepository.findByExternalId(documentId).ifPresentOrElse(documentRepository::delete, () -> log.warn("Documento {} não encontrado para remoção.", documentId));
    }

    private ProviderSignature resolveProviderForDocument(String documentId, ProviderSignature provider) {
        if (provider != null) return provider;
        return documentRepository.findByExternalId(documentId)
                .map(doc -> doc.getEnvelope().getProvider()).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Provider não informado e documento não encontrado localmente: " + documentId));
    }

    // ── Signatário ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<Signer> addSigners(String externalId, List<AddSignerCommand> commands, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Adicionando {} signatários ao envelope {} no provedor {}", commands.size(), externalId, resolvedProvider);
        ESignatureGateway gateway = registry.get(resolvedProvider);

        List<Signer> signers = new ArrayList<>();
        for (AddSignerCommand cmd : commands) {
            Signer signer = gateway.addSigner(externalId, cmd);
            if (signer != null) signers.add(signer);
        }

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            for (int i = 0; i < signers.size(); i++) {
                Signer signer = signers.get(i);
                AddSignerCommand cmd = commands.get(i);

                SignerEntity entity = new SignerEntity();
                entity.setExternalId(signer.getExternalId());
                entity.setName(signer.getName());
                entity.setEmail(cmd.email());
                entity.setEnvelope(envelope);
                entity.setCreated(LocalDateTime.now());
                signerRepository.save(entity);
            }
        });

        return signers;
    }

    @Override
    public List<Signer> getSigners(String externalId, ProviderSignature provider) {
        log.info("Listando signatários do envelope {} no banco local", externalId);
        return signerRepository.findAllByEnvelopeExternalId(externalId)
                .stream()
                .map(entity -> Signer.builder()
                        .externalId(entity.getExternalId())
                        .name(entity.getName())
                        .email(entity.getEmail())
                        .status(entity.getStatus())
                        .signedAt(entity.getSignedAt() != null ? entity.getSignedAt().atOffset(ZoneOffset.UTC) : null)
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Signer getSigner(String externalId, String signerId, ProviderSignature provider) {
        return signerRepository.findByExternalId(signerId)
                .map(this::mapToSignerModel).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Signatário não encontrado localmente: " + signerId));
    }

    @Override
    @Transactional
    public void deleteSigner(String externalId, String signerId, ProviderSignature provider) {
        // Operação ainda não suportada no gateway (Provider)
        signerRepository.findByExternalId(signerId).ifPresentOrElse(signerRepository::delete, () -> log.warn("Signatário {} não encontrado para remoção.", signerId));
    }

    private ProviderSignature resolveProviderForSigner(String signerId, ProviderSignature provider) {
        if (provider != null) return provider;
        return signerRepository.findByExternalId(signerId)
                .map(s -> s.getEnvelope().getProvider())
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Provider não informado e signatário não encontrado localmente: " + signerId));
    }

    // ── Requisito ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, resolvedProvider);
        Requirement requirement = registry.get(resolvedProvider).addRequirement(externalId, cmd);

        if (requirement == null) {
            log.warn("Requisito retornou nulo do provider. Ignorando persistência local.");
            return;
        }

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            var signerOpt = signerRepository.findByExternalId(cmd.signerId());
            var docOpt    = documentRepository.findByExternalId(cmd.documentId());

            if (signerOpt.isPresent() && docOpt.isPresent()) {
                RequirementEntity entity = new RequirementEntity();
                entity.setExternalId(requirement.getExternalId());
                entity.setEnvelope(envelope);
                entity.setSigner(signerOpt.get());
                entity.setDocument(docOpt.get());
                entity.setCreated(LocalDateTime.now());
                requirementRepository.save(entity);
                log.info("Requisito {} persistido localmente.", requirement.getExternalId());
            } else {
                log.warn("Requisito não persistido: signatário ou documento não encontrado.");
            }
        });
    }

    @Override
    public List<Requirement> getRequirements(String externalId, ProviderSignature provider) {
        log.info("Listando requisitos do envelope {} no banco local", externalId);
        return requirementRepository.findAllByEnvelopeExternalId(externalId)
                .stream()
                .map(entity -> Requirement.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Requirement getRequirement(String requirementId, ProviderSignature provider) {
        return requirementRepository.findByExternalId(requirementId)
                .map(entity -> Requirement.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Requisito não encontrado localmente: " + requirementId));
    }

    @Override
    @Transactional
    public void deleteRequirement(String requirementId, ProviderSignature provider) {
        // Operação ainda não suportada no gateway (Provider)
        requirementRepository.findByExternalId(requirementId).ifPresentOrElse(requirementRepository::delete, () -> log.warn("Requisito {} não encontrado para remoção.", requirementId));
    }

    private ProviderSignature resolveProviderForRequirement(String requirementId, ProviderSignature provider) {
        if (provider != null) return provider;
        return requirementRepository.findByExternalId(requirementId)
                .map(r -> r.getEnvelope().getProvider())
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Provider não informado e requisito não encontrado localmente: " + requirementId));
    }

    @Override
    @Transactional
    public void remindSigner(String externalId, String signerId, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        var signer = signerRepository.findByExternalId(signerId).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Signatário não encontrado"));

        if (signer.getLastRemindedAt() != null && signer.getLastRemindedAt().isAfter(LocalDateTime.now().minusHours(1))) {
            throw new DomainException(DomainErrorCode.REMINDER_RATE_LIMIT, "Um lembrete já foi enviado na última hora para este signatário");
        }

        registry.get(resolvedProvider).remindSigner(externalId, signerId);

        signer.setLastRemindedAt(LocalDateTime.now());
        signerRepository.save(signer);

        log.info("Lembrete enviado para o signatário {} do envelope {} via {}", signerId, externalId, resolvedProvider);
    }

    @Override
    @Transactional
    public void addNotifier(String externalId, AddNotifierCommand cmd, ProviderSignature provider) {
        ProviderSignature resolvedProvider = resolveProviderForEnvelope(externalId, provider);
        log.info("Adicionando observador {} ao envelope {} via {}", cmd.email(), externalId, resolvedProvider);

        EnvelopeEntity envelope = repository.findByExternalId(externalId).orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Envelope não encontrado"));

        String notifierExternalId = registry.get(resolvedProvider).addNotifier(externalId, cmd);

        NotifierEntity notifier = NotifierEntity.builder()
                .envelope(envelope)
                .email(cmd.email())
                .name(cmd.name())
                .externalId(notifierExternalId)
                .build();

        notifierRepository.save(notifier);
    }

    @Override
    public List<OutboundWebhookDeliveryResponse> getWebhookDeliveries(String externalId) {
        log.info("Buscando histórico de entregas de webhook para envelope {}", externalId);
        return outboundWebhookDeliveryRepository.findByEnvelopeExternalIdOrderByCreatedAtDesc(externalId)
                .stream()
                .map(entity -> OutboundWebhookDeliveryResponse.builder()
                        .id(entity.getId())
                        .url(entity.getUrl())
                        .payload(entity.getPayload())
                        .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                        .attempts(entity.getAttempts())
                        .httpStatusCode(entity.getHttpStatusCode())
                        .lastAttemptAt(entity.getLastAttemptAt())
                        .createdAt(entity.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMe() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("[LGPD] Processando solicitação de exclusão (soft delete) para o usuário: {}", username);

        userRepository.findByUsername(username).ifPresent(user -> {
            String originalId = String.valueOf(user.getId());
            user.setDeleted_at(LocalDateTime.now());
            
            // Anonimização para LGPD (Direito ao Esquecimento)
            user.setUsername("deleted_" + originalId);
            user.setEmail("deleted_" + originalId + "@signflow.com");
            
            userRepository.save(user);
            auditLogService.log("DELETE_ME", "USER", originalId, "Usuário marcou sua conta como deletada e dados foram anonimizados");
            log.info("[AUDIT] Usuário {} marcou sua conta como deletada (Direito ao Esquecimento). Dados anonimizados.", originalId);
        });
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private void updateStatus(EnvelopeEntity entity, Status newStatus) {
        Status previous = entity.getStatus();
        if (previous != newStatus) {
            entity.setStatus(newStatus);
            repository.save(entity);
            saveEvent(entity, previous, newStatus);
        }
    }

    private void saveEvent(EnvelopeEntity entity, Status previous, Status next) {
        EnvelopeEventEntity event = new EnvelopeEventEntity();
        event.setEnvelope(entity);
        event.setPreviousStatus(previous);
        event.setNewStatus(next);
        event.setSource("API");
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);

        // Notificar via Kafka sobre a mudança de status (Outbound Webhook)
        NormalizedWebhookEvent normalizedEvent = NormalizedWebhookEvent.builder()
                .envelopeExternalId(entity.getExternalId())
                .provider(entity.getProvider())
                .eventType(mapStatusToEventType(next))
                .occurredAt(event.getOccurredAt())
                .providerStatus(next.name())
                .build();

        kafkaTemplate.send(KafkaConfig.ENVELOPE_EVENTS_TOPIC, entity.getExternalId(), normalizedEvent);
    }

    private com.signflow.enums.WebhookEventType mapStatusToEventType(Status status) {
        return switch (status) {
            case DRAFT -> com.signflow.enums.WebhookEventType.DOCUMENT_CREATED;
            case PENDING, ACTIVE -> com.signflow.enums.WebhookEventType.DOCUMENT_SENT;
            case CLOSED -> com.signflow.enums.WebhookEventType.DOCUMENT_COMPLETED;
            case CANCELED, DELETED -> com.signflow.enums.WebhookEventType.DOCUMENT_CANCELED;
            case EXPIRED -> com.signflow.enums.WebhookEventType.DOCUMENT_EXPIRED;
            case REFUSED -> com.signflow.enums.WebhookEventType.DOCUMENT_REJECTED;
            default -> com.signflow.enums.WebhookEventType.UNKNOWN;
        };
    }

    private Signer mapToSignerModel(SignerEntity entity) {
        return Signer.builder()
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .email(entity.getEmail())
                .status(entity.getStatus())
                .signedAt(entity.getSignedAt() != null ? entity.getSignedAt().atOffset(ZoneOffset.UTC) : null)
                .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                .build();
    }
}
