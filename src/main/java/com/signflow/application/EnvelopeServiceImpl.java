package com.signflow.application;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.SignatureGatewayRegistry;
import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.persistence.*;
import com.signflow.api.dto.EnvelopeTimelineResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvelopeServiceImpl implements EnvelopeService {

    private final SignatureGatewayRegistry registry;
    private final SignatureRepository repository;
    private final SignerRepository signerRepository;
    private final DocumentRepository documentRepository;
    private final RequirementRepository requirementRepository;
    private final EnvelopeEventRepository eventRepository;

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

            updateStatus(entity, Status.SUCCESS, "API");
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

    @Override
    @Transactional
    public List<Signer> addSigners(String externalId, List<AddSignerCommand> commands, ProviderSignature provider) {
        log.info("Adicionando {} signatários ao envelope {} no provedor {}", commands.size(), externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        List<Signer> signers = gateway.addSigners(externalId, commands);

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
    public Envelope getEnvelope(String externalId, ProviderSignature provider) {
        return repository.findByExternalId(externalId)
                .map(entity -> {
                    log.info("Envelope {} encontrado no banco local.", externalId);
                    return Envelope.builder()
                            .id(entity.getId().toString())
                            .externalId(entity.getExternalId())
                            .name(entity.getName())
                            .status(entity.getStatus())
                            .created(entity.getCreated() != null ? entity.getCreated().atOffset(ZoneOffset.UTC) : null)
                            .build();
                })
                .orElseGet(() -> {
                    log.info("Envelope {} não encontrado localmente. Consultando provedor {}.", externalId, provider);
                    ESignatureGateway gateway = registry.get(provider);
                    return gateway.getEnvelope(externalId);
                });
    }

    @Override
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

    @Override
    @Transactional
    public Requirement addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        Requirement requirement = gateway.addRequirement(externalId, cmd);

        repository.findByExternalId(externalId).ifPresent(envelope -> {
            var signerOpt = signerRepository.findByExternalId(cmd.signerId());
            var docOpt = documentRepository.findByExternalId(cmd.documentId());

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
                log.warn("Não foi possível persistir o requisito localmente: signatário ou documento não encontrado.");
            }
        });

        return requirement;
    }


    @Override
    @Transactional
    public void activateEnvelope(String externalId, ProviderSignature provider) {
        log.info("Ativando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.activateEnvelope(externalId);
        
        repository.findByExternalId(externalId).ifPresent(entity -> {
            updateStatus(entity, Status.ACTIVE, "API");
        });
    }

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

    @Override
    public Page<Envelope> listEnvelopes(Status status, Pageable pageable) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("Listando envelopes para o usuário {} com status {} e paginação {}", userId, status, pageable);

        Page<EnvelopeEntity> entities;
        if (status != null) {
            entities = repository.findAllByUserIdAndStatus(userId, status, pageable);
        } else {
            entities = repository.findAllByUserId(userId, pageable);
        }

        return entities.map(entity -> Envelope.builder()
                .id(entity.getId().toString())
                .externalId(entity.getExternalId())
                .name(entity.getName())
                .status(entity.getStatus())
                .created(entity.getCreated().atOffset(ZoneOffset.UTC))
                .build());
    }

    @Override
    @Transactional
    public void activateEnvelopeComplete(ProviderSignature provider, String externalId, ActivateEnvelopeCommand command) {
        log.info("Iniciando ativação completa do envelope {} no provedor {}", externalId, provider);

        EnvelopeEntity envelope = repository.findByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Envelope não encontrado: " + externalId));

        SignerEntity signer = signerRepository.findByExternalId(command.signerId())
                .orElseThrow(() -> new RuntimeException("Signatário não encontrado: " + command.signerId()));

        DocumentEntity document = documentRepository.findByExternalId(command.documentId())
                .orElseThrow(() -> new RuntimeException("Documento não encontrado: " + command.documentId()));

//        // Validação de vínculo (opcional, mas recomendado)
//        if (!signer.getEnvelope().getId().equals(envelope.getId())) {
//            throw new RuntimeException("Signatário não pertence ao envelope informado");
//        }
//        if (!document.getEnvelope().getId().equals(envelope.getId())) {
//            throw new RuntimeException("Documento não pertence ao envelope informado");
//        }

        String role = command.role() != null ? command.role() : "sign";
        String auth = command.auth() != null ? command.auth() : "email";

        ESignatureGateway gateway = registry.get(provider);

        // ── 1. Requisito de Qualificação ─────────────────────────────
        AddRequirementCommand qualification = AddRequirementCommand.builder()
                .signerId(signer.getExternalId())
                .documentId(document.getExternalId())
                .action("agree")
                .role(role)
                .build();

        log.info("Criando requisito de qualificação para o envelope {}", externalId);
        Requirement qualificationReq = gateway.addRequirement(externalId, qualification);
        log.info("Requisito de qualificação criado: {}", qualificationReq.getExternalId());

        RequirementEntity qReqEntity = new RequirementEntity();
        qReqEntity.setExternalId(qualificationReq.getExternalId());
        qReqEntity.setEnvelope(envelope);
        qReqEntity.setSigner(signer);
        qReqEntity.setDocument(document);
        qReqEntity.setCreated(LocalDateTime.now());
        requirementRepository.save(qReqEntity);

        // ── 2. Requisito de Autenticação ──────────────────────────────
        AddRequirementCommand authentication = AddRequirementCommand.builder()
                .signerId(signer.getExternalId())
                .documentId(document.getExternalId())
                .action("provide_evidence")
                .auth(auth)
                .build();

        log.info("Criando requisito de autenticação para o envelope {}", externalId);
        Requirement authenticationReq = gateway.addRequirement(externalId, authentication);
        log.info("Requisito de autenticação criado: {}", authenticationReq.getExternalId());

        RequirementEntity aReqEntity = new RequirementEntity();
        aReqEntity.setExternalId(authenticationReq.getExternalId());
        aReqEntity.setEnvelope(envelope);
        aReqEntity.setSigner(signer);
        aReqEntity.setDocument(document);
        aReqEntity.setCreated(LocalDateTime.now());
        requirementRepository.save(aReqEntity);

        // ── 3. Ativar o envelope no provider ──────────────────────────
        gateway.activateEnvelope(externalId);
        log.info("Envelope {} ativado no provider {}", externalId, provider);

        // ── 4. Atualizar status no banco local ────────────────────────
        updateStatus(envelope, Status.ACTIVE, "API");
    }

    private void updateStatus(EnvelopeEntity entity, Status newStatus, String source) {
        Status previousStatus = entity.getStatus();
        if (previousStatus != newStatus) {
            entity.setStatus(newStatus);
            repository.save(entity);
            saveEvent(entity, previousStatus, newStatus, source);
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


}
