package com.signflow.application;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.SignatureGatewayRegistry;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvelopeServiceImpl implements EnvelopeService {

    private final SignatureGatewayRegistry registry;
    private final SignatureRepository repository;
    private final SignerRepository signerRepository;
    private final DocumentRepository documentRepository;
    private final EnvelopeEventRepository eventRepository;

    @Override
    @Transactional
    public Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Iniciando criação de envelope para o provedor {}", provider);

        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setStatus(Status.PROCESSING);
        entity.setProvider(provider);
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
    public Signer addSigner(String externalId, AddSignerCommand cmd, ProviderSignature provider) {
        log.info("Adicionando signatário ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        Signer signer = gateway.addSigner(externalId, cmd);
        
        repository.findByExternalId(externalId).ifPresent(envelope -> {
            SignerEntity signerEntity = new SignerEntity();
            signerEntity.setExternalId(signer.getExternalId());
            signerEntity.setName(signer.getName());
            signerEntity.setEmail(cmd.getEmail());
            signerEntity.setEnvelope(envelope);
            signerEntity.setCreated(LocalDateTime.now());
            signerRepository.save(signerEntity);
        });
        
        return signer;
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
            documentEntity.setFilename(cmd.getFilename());
            documentEntity.setEnvelope(envelope);
            documentEntity.setCreated(LocalDateTime.now());
            documentRepository.save(documentEntity);
        });
        
        return document;
    }

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider) {
        ESignatureGateway gateway = registry.get(provider);
        return gateway.getEnvelope(externalId);
    }

    @Override
    public Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Atualizando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        return gateway.updateEnvelope(externalId, cmd);
    }

    @Override
    public void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.addRequirement(externalId, cmd);
    }

    @Override
    @Transactional
    public void activateEnvelope(String externalId, ProviderSignature provider) {
        log.info("Ativando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.activateEnvelope(externalId);
        
        repository.findByExternalId(externalId).ifPresent(envelope -> {
            updateStatus(envelope, Status.ACTIVE, "API");
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
