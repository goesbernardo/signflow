package com.signflow.application.service.impl;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.application.port.in.SignatureService;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.application.port.out.ESignatureGateway;
import com.signflow.domain.command.*;

import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.*;
import com.signflow.infrastructure.gateway.SignatureGatewayRegistry;
import com.signflow.infrastructure.persistence.entity.*;
import com.signflow.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ── createEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Iniciando criação de envelope para o provedor {}", provider);

        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setStatus(Status.PROCESSING);
        entity.setProvider(provider);
        entity.setName(cmd.name());
        entity.setCreated(LocalDateTime.now());

        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setUserId(userId);

        entity = repository.save(entity);
        saveEvent(entity, null, Status.PROCESSING, "API");

        try {
            ESignatureGateway gateway = registry.get(provider);
            Envelope envelope = gateway.createEnvelope(cmd);

            updateStatus(entity, Status.DRAFT, "API");
            entity.setExternalId(envelope.getExternalId());
            repository.save(entity);

            envelope.setId(entity.getId().toString());
            return envelope;
        } catch (Exception e) {
            log.error("Erro ao criar envelope no provedor {}", provider, e);
            updateStatus(entity, Status.FAILED, "API");
            throw e;
        }
    }

    // ── updateEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Atualizando envelope {} no provedor {}", externalId, provider);
        Envelope envelope = registry.get(provider).updateEnvelope(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(entity -> {
            entity.setName(cmd.name());
            repository.save(entity);
        });

        return envelope;
    }

    // ── getEnvelope ───────────────────────────────────────────────────────

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider, boolean includeSigners) {
        return repository.findByExternalId(externalId)
                .map(entity -> {
                    log.info("Envelope {} encontrado no banco local.", externalId);
                    Envelope envelope = Envelope.builder()
                            .id(entity.getId().toString())
                            .externalId(entity.getExternalId())
                            .name(entity.getName())
                            .status(entity.getStatus())
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
                    log.info("Envelope {} não encontrado localmente. Consultando {}.", externalId, provider);
                    return registry.get(provider).getEnvelope(externalId);
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
        log.info("Ativando envelope {} no provedor {}", externalId, provider);
        
        EnvelopeEntity entity = repository.findByExternalId(externalId)
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Envelope não encontrado: " + externalId));

        if (entity.getStatus() != Status.DRAFT) {
            throw new DomainException(DomainErrorCode.INVALID_ENVELOPE_STATUS, "Somente envelopes em rascunho (DRAFT) podem ser ativados. Status atual: " + entity.getStatus());
        }

        registry.get(provider).activateEnvelope(externalId);
        updateStatus(entity, Status.ACTIVE, "API");
    }

    // ── cancelEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelEnvelope(String externalId, ProviderSignature provider) {
        log.info("Cancelando envelope {} no provedor {}", externalId, provider);
        
        EnvelopeEntity entity = repository.findByExternalId(externalId)
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Envelope não encontrado: " + externalId));

        if (entity.getStatus() != Status.ACTIVE && entity.getStatus() != Status.DRAFT) {
            throw new DomainException(DomainErrorCode.INVALID_ENVELOPE_STATUS, "Somente envelopes ACTIVE ou DRAFT podem ser cancelados. Status atual: " + entity.getStatus());
        }

        registry.get(provider).cancelEnvelope(externalId);
        updateStatus(entity, Status.CANCELED, "API");
    }

    // ── listEnvelopes ─────────────────────────────────────────────────────

    @Override
    public Page<Envelope> listEnvelopes(Status status, Pageable pageable, boolean includeSigners) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Listando envelopes para o usuário {} com status {} (includeSigners={})", userId, status, includeSigners);

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
        return eventRepository.findAllByEnvelopeExternalIdOrderByOccurredAtAsc(externalId)
                .stream()
                .map(event -> EnvelopeTimelineResponse.builder()
                        .previousStatus(event.getPreviousStatus())
                        .newStatus(event.getNewStatus())
                        .source(event.getSource())
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
        log.info("Iniciando criação de envelope completo: {}", cmd.name());

        // 1. Criar Envelope
        Envelope envelope = createEnvelope(CreateEnvelopeCommand.builder().name(cmd.name()).build(), provider);
        String envelopeId = envelope.getExternalId();
        log.info("Envelope criado com ID: {}", envelopeId);

        // 2. Adicionar Documentos
        List<String> documentIds = new ArrayList<>();
        if (cmd.documents() != null) {
            for (AddDocumentCommand docCmd : cmd.documents()) {
                Document doc = addDocument(envelopeId, docCmd, provider);
                documentIds.add(doc.getExternalId());
            }
        }
        log.info("Documentos adicionados: {}", documentIds);

        // 3. Adicionar Signatários
        List<String> signerIds = new ArrayList<>();
        if (cmd.signers() != null) {
            List<Signer> signers = addSigners(envelopeId, cmd.signers(), provider);
            signers.forEach(s -> signerIds.add(s.getExternalId()));
        }
        log.info("Signatários adicionados: {}", signerIds);

        // 4. Adicionar Requisitos
        // FullRequirementCommand agora usa SignerRole e SignatureAuthMethod diretamente.
        // Sem conversão intermediária — os valores chegam já no formato do domínio neutro.
        if (cmd.requirements() != null && !documentIds.isEmpty() && !signerIds.isEmpty()) {
            for (FullRequirementCommand reqCmd : cmd.requirements()) {

                // Default: SIGN se role não informado
                SignerRole role = reqCmd.role() != null ? reqCmd.role() : SignerRole.SIGN;

                // Default: EMAIL se auth não informado
                SignatureAuthMethod auth = reqCmd.auth() != null ? reqCmd.auth() : SignatureAuthMethod.EMAIL;

                for (String signerId : signerIds) {
                    for (String documentId : documentIds) {

                        // 4a. Requisito de Qualificação — role
                        addRequirement(envelopeId, AddRequirementCommand.builder()
                                .signerId(signerId)
                                .documentId(documentId)
                                .role(role)
                                .build(), provider);

                        // 4b. Requisito de Autenticação — auth
                        addRequirement(envelopeId, AddRequirementCommand.builder()
                                .signerId(signerId)
                                .documentId(documentId)
                                .auth(auth)
                                .build(), provider);
                    }
                }
            }
        }
        log.info("Requisitos adicionados");

        // 5. Ativação automática
        if (Boolean.TRUE.equals(cmd.autoActivate())) {
            activateEnvelope(envelopeId, provider);
            log.info("Envelope {} ativado automaticamente", envelopeId);
        }

        return getEnvelope(envelopeId, provider);
    }

    // ── Documento ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider) {
        log.info("Adicionando documento ao envelope {} no provedor {}", externalId, provider);
        Document document = registry.get(provider).addDocument(externalId, cmd);

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
        documentRepository.findByExternalId(documentId).ifPresentOrElse(documentRepository::delete, () -> log.warn("Documento {} não encontrado para remoção.", documentId));
    }

    // ── Signatário ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<Signer> addSigners(String externalId, List<AddSignerCommand> commands, ProviderSignature provider) {
        log.info("Adicionando {} signatários ao envelope {} no provedor {}", commands.size(), externalId, provider);
        ESignatureGateway gateway = registry.get(provider);

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
        return signerRepository.findAllByEnvelopeExternalId(externalId)
                .stream()
                .map(entity -> Signer.builder()
                        .externalId(entity.getExternalId())
                        .name(entity.getName())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Signer getSigner(String externalId, String signerId, ProviderSignature provider) {
        return signerRepository.findByExternalId(signerId)
                .map(this::mapToSignerModel)
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Signatário não encontrado: " + signerId));
    }

    @Override
    @Transactional
    public void deleteSigner(String externalId, String signerId, ProviderSignature provider) {
        signerRepository.findByExternalId(signerId).ifPresentOrElse(signerRepository::delete, () -> log.warn("Signatário {} não encontrado para remoção.", signerId));
    }

    // ── Requisito ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, provider);
        Requirement requirement = registry.get(provider).addRequirement(externalId, cmd);

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
        return requirementRepository.findAllByEnvelopeExternalId(externalId)
                .stream()
                .map(entity -> Requirement.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null
                                ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Requirement getRequirement(String requirementId, ProviderSignature provider) {
        return requirementRepository.findByExternalId(requirementId)
                .map(entity -> Requirement.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null
                                ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Requisito não encontrado: " + requirementId));
    }

    @Override
    @Transactional
    public void deleteRequirement(String requirementId, ProviderSignature provider) {
        requirementRepository.findByExternalId(requirementId).ifPresentOrElse(requirementRepository::delete, () -> log.warn("Requisito {} não encontrado para remoção.", requirementId));
    }

    @Override
    @Transactional
    public void remindSigner(String externalId, String signerId, ProviderSignature provider) {
        var signer = signerRepository.findByExternalId(signerId)
                .orElseThrow(() -> new DomainException(DomainErrorCode.NOT_FOUND, "Signatário não encontrado"));

        if (signer.getLastRemindedAt() != null &&
                signer.getLastRemindedAt().isAfter(LocalDateTime.now().minusHours(1))) {
            throw new DomainException(DomainErrorCode.REMINDER_RATE_LIMIT, "Um lembrete já foi enviado na última hora para este signatário");
        }

        registry.get(provider).remindSigner(externalId, signerId);

        signer.setLastRemindedAt(LocalDateTime.now());
        signerRepository.save(signer);

        log.info("Lembrete enviado para o signatário {} do envelope {}", signerId, externalId);
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private void updateStatus(EnvelopeEntity entity, Status newStatus, String source) {
        Status previous = entity.getStatus();
        if (previous != newStatus) {
            entity.setStatus(newStatus);
            repository.save(entity);
            saveEvent(entity, previous, newStatus, source);
        }
    }

    private void saveEvent(EnvelopeEntity entity, Status previous, Status next, String source) {
        EnvelopeEventEntity event = new EnvelopeEventEntity();
        event.setEnvelope(entity);
        event.setPreviousStatus(previous);
        event.setNewStatus(next);
        event.setSource(source);
        event.setOccurredAt(LocalDateTime.now());
        eventRepository.save(event);
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
