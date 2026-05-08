package com.signflow.application.service.impl;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.application.port.in.SignatureService;
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

        String currentPrincipalName = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setUserId(currentPrincipalName);

        entity = repository.save(entity);
        saveEvent(entity, null, Status.PROCESSING, "API");

        try {
            ESignatureGateway gateway = registry.get(provider);
            Envelope envelope = gateway.createEnvelope(cmd);

            updateStatus(entity, Status.SUCCESS);
            entity.setExternalId(envelope.getExternalId());
            repository.save(entity);

            envelope.setId(entity.getId().toString());
            return envelope;
        } catch (Exception e) {
            log.error("Erro ao criar envelope no provedor {}", provider, e);
            updateStatus(entity, Status.FAILED);
            throw e;
        }
    }

    // ── updateEnvelope ────────────────────────────────────────────────────

    @Override
    @Transactional
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Atualizando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        Envelope envelope = gateway.updateEnvelope(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(entity -> {
            entity.setName(cmd.name());
            repository.save(entity);
        });

        return envelope;
    }

    // ── getEnvelope ───────────────────────────────────────────────────────

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider) {
        return repository.findByExternalId(externalId)
                .map(entity -> {
                    log.info("Envelope {} encontrado no banco local.", externalId);
                    return Envelope.builder()
                            .id(entity.getId().toString())
                            .externalId(entity.getExternalId())
                            .name(entity.getName())
                            .status(entity.getStatus())
                            .created(entity.getCreated() != null
                                    ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                            .build();
                })
                .orElseGet(() -> {
                    log.info("Envelope {} não encontrado localmente. Consultando provedor {}.", externalId, provider);
                    return registry.get(provider).getEnvelope(externalId);
                });
    }

    // ── activateEnvelope ──────────────────────────────────────────────────

    @Override
    @Transactional
    public void activateEnvelope(String externalId, ProviderSignature provider) {
        log.info("Ativando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.activateEnvelope(externalId);

        repository.findByExternalId(externalId).ifPresent(entity -> updateStatus(entity, Status.ACTIVE));
    }

    // ── listEnvelopes ─────────────────────────────────────────────────────

    @Override
    public Page<Envelope> listEnvelopes(Status status, Pageable pageable) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Listando envelopes para o usuário {} com status {}", userId, status);

        Page<EnvelopeEntity> entities = status != null
                ? repository.findAllByUserIdAndStatus(userId, status, pageable)
                : repository.findAllByUserId(userId, pageable);

        return entities.map(entity -> Envelope.builder()
                .id(entity.getId().toString())
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .status(entity.getStatus())
                .created(entity.getCreated().atOffset(ZoneOffset.UTC))
                .build());
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

    @Override
    @Transactional
    public Envelope createFullEnvelope(CreateFullEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Iniciando criação de envelope: {}", cmd.name());

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

        // 4. Adicionar Requisitos (qualificação + autenticação por signatário × documento)
        if (cmd.requirements() != null && !documentIds.isEmpty() && !signerIds.isEmpty()) {
            for (FullRequirementCommand reqCmd : cmd.requirements()) {

                SignerRole         role = mapToSignerRole(reqCmd.role());
                SignatureAuthMethod auth = mapToAuthMethod(reqCmd.auth());

                for (String signerId : signerIds) {
                    for (String documentId : documentIds) {

                        if (role != null) {
                            addRequirement(envelopeId, AddRequirementCommand.builder()
                                    .signerId(signerId)
                                    .documentId(documentId)
                                    .role(role)
                                    .build(), provider);
                        }

                        if (auth != null) {
                            addRequirement(envelopeId, AddRequirementCommand.builder()
                                    .signerId(signerId)
                                    .documentId(documentId)
                                    .auth(auth)
                                    .build(), provider);
                        }
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
        ESignatureGateway gateway = registry.get(provider);
        Document document = gateway.addDocument(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            DocumentEntity documentEntity = new DocumentEntity();
            documentEntity.setExternalId(document.getExternalId());
            documentEntity.setFilename(cmd.filename());
            documentEntity.setEnvelope(envelope);
            documentEntity.setCreated(LocalDateTime.now());
            documentRepository.save(documentEntity);
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
                        .created(entity.getCreated() != null
                                ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Document getDocument(String documentId, ProviderSignature provider) {
        log.info("Buscando documento {} no banco local", documentId);
        return documentRepository.findByExternalId(documentId)
                .map(entity -> Document.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null
                                ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException("Documento não encontrado: " + documentId));
    }

    @Override
    public Document updateDocument(String documentId, UpdateDocumentCommand cmd, ProviderSignature provider) {
        log.info("Atualizando documento {} no banco local", documentId);
        DocumentEntity entity = documentRepository.findByExternalId(documentId)
                .orElseThrow(() -> new DomainException("Documento não encontrado: " + documentId));

        if (cmd.filename() != null) {
            entity.setFilename(cmd.filename());
        }
        documentRepository.save(entity);

        return Document.builder()
                .externalId(entity.getExternalId())
                .created(entity.getCreated() != null
                        ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                .build();
    }

    @Override
    @Transactional
    public void deleteDocument(String documentId, ProviderSignature provider) {
        log.info("Removendo documento {} no banco local", documentId);
        documentRepository.findByExternalId(documentId).ifPresentOrElse(documentRepository::delete, () -> log.warn("Documento {} não encontrado localmente para remoção.", documentId));
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
            if (signer != null) {
                signers.add(signer);
            }
        }

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            for (int i = 0; i < signers.size(); i++) {
                Signer signer = signers.get(i);
                AddSignerCommand cmd = commands.get(i);

                SignerEntity signerEntity = new SignerEntity();
                signerEntity.setExternalId(signer.getExternalId());
                signerEntity.setName(signer.getName());
                signerEntity.setEmail(cmd.email());
                signerEntity.setEnvelope(envelope);
                signerEntity.setCreated(LocalDateTime.now());
                signerRepository.save(signerEntity);
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
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build()).collect(Collectors.toList());
    }

    @Override
    public Signer getSigner(String externalId, String signerId, ProviderSignature provider) {
        log.info("Buscando signatário {} do envelope {} no banco local", signerId, externalId);
        return signerRepository.findByExternalId(signerId)
                .map(entity -> Signer.builder()
                        .externalId(entity.getExternalId())
                        .name(entity.getName())
                        .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException("Signatário não encontrado: " + signerId));
    }

    @Override
    @Transactional
    public void deleteSigner(String externalId, String signerId, ProviderSignature provider) {
        log.info("Removendo signatário {} do envelope {} no banco local", signerId, externalId);
        signerRepository.findByExternalId(signerId).ifPresentOrElse(signerRepository::delete, () -> log.warn("Signatário {} não encontrado localmente para remoção.", signerId));
    }

    // ── Requisito ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        Requirement requirement = gateway.addRequirement(externalId, cmd);

        if (requirement == null) {
            log.warn("Falha ao criar requisito no provider: retorno nulo.");
            return;
        }

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            var signerOpt = signerRepository.findByExternalId(cmd.signerId());
            var docOpt    = documentRepository.findByExternalId(cmd.documentId());

            if (signerOpt.isPresent() && docOpt.isPresent()) {
                RequirementEntity requirementEntity = new RequirementEntity();
                requirementEntity.setExternalId(requirement.getExternalId());
                requirementEntity.setEnvelope(envelope);
                requirementEntity.setSigner(signerOpt.get());
                requirementEntity.setDocument(docOpt.get());
                requirementEntity.setCreated(LocalDateTime.now());
                requirementRepository.save(requirementEntity);
                log.info("Requisito {} persistido localmente.", requirement.getExternalId());
            } else {
                log.warn("Requisito não persistido localmente: signatário ou documento não encontrado.");
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
        log.info("Buscando requisito {} no banco local", requirementId);
        return requirementRepository.findByExternalId(requirementId)
                .map(entity -> Requirement.builder()
                        .externalId(entity.getExternalId())
                        .created(entity.getCreated() != null
                                ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                        .build())
                .orElseThrow(() -> new DomainException("Requisito não encontrado: " + requirementId));
    }

    @Override
    @Transactional
    public void deleteRequirement(String requirementId, ProviderSignature provider) {
        log.info("Removendo requisito {} do banco local", requirementId);
        requirementRepository.findByExternalId(requirementId).ifPresentOrElse(requirementRepository::delete, () -> log.warn("Requisito {} não encontrado localmente para remoção.", requirementId));
    }

    // ── Helpers privados ──────────────────────────────────────────────────

    private void updateStatus(EnvelopeEntity entity, Status newStatus) {
        Status previousStatus = entity.getStatus();
        if (previousStatus != newStatus) {
            entity.setStatus(newStatus);
            repository.save(entity);
            saveEvent(entity, previousStatus, newStatus, "API");
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

    /**
     * Mapeia RequirementRole (enum da ClickSign/API) para SignerRole (domínio neutro).
     * Usado no createFullEnvelope para traduzir o input do consumidor.
     */
    private SignerRole mapToSignerRole(RequirementRole role) {
        if (role == null) return null;
        return switch (role) {
            case RECEIPT     -> SignerRole.PARTY;
            case CONTRACTOR  -> SignerRole.CONTRACTOR;
            case INTERVENING -> SignerRole.INTERVENING;
            default          -> SignerRole.SIGN;
        };
    }

    private SignatureAuthMethod mapToAuthMethod(RequirementAuth auth) {
        if (auth == null) return null;
        return switch (auth) {
            case SMS               -> SignatureAuthMethod.SMS;
            case WHATSAPP          -> SignatureAuthMethod.WHATSAPP;
            case PIX               -> SignatureAuthMethod.PIX;
            case HANDWRITTEN       -> SignatureAuthMethod.HANDWRITTEN;
            case FACIAL_BIOMETRICS -> SignatureAuthMethod.FACIAL_BIOMETRICS;
            case API               -> SignatureAuthMethod.API;
            case AUTO_SIGNATURE    -> SignatureAuthMethod.AUTO;
            default                -> SignatureAuthMethod.EMAIL;
        };
    }
}
