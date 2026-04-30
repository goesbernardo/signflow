package com.signflow.application;

import com.signflow.adapter.ESignatureGateway;
import com.signflow.adapter.SignatureGatewayRegistry;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.persistence.EnvelopeEntity;
import com.signflow.persistence.SignatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvelopeServiceImpl implements EnvelopeService {

    private final SignatureGatewayRegistry registry;
    private final SignatureRepository repository;

    @Override
    @Transactional
    public Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider) {
        log.info("Iniciando criação de envelope para o provedor {}", provider);

        EnvelopeEntity entity = new EnvelopeEntity();
        entity.setStatus(Status.PROCESSING);
        entity.setProvider(provider);
        entity.setCreated(LocalDateTime.now());
        entity = repository.save(entity);

        try {
            ESignatureGateway gateway = registry.get(provider);
            Envelope envelope = gateway.createEnvelope(cmd);

            entity.setStatus(Status.SUCCESS);
            entity.setExternalId(envelope.getExternalId());
            repository.save(entity);

            return envelope;
        } catch (Exception e) {
            log.error("Erro ao criar envelope no provedor {}", provider, e);
            entity.setStatus(Status.FAILED);
            repository.save(entity);
            throw e;
        }
    }

    @Override
    public Envelope getEnvelope(String externalId, ProviderSignature provider) {
        ESignatureGateway gateway = registry.get(provider);
        return gateway.getEnvelope(externalId);
    }

    @Override
    public Signer addSigner(String externalId, AddSignerCommand cmd, ProviderSignature provider) {
        log.info("Adicionando signatário ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        return gateway.addSigner(externalId, cmd);
    }

    @Override
    public Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider) {
        log.info("Adicionando documento ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        return gateway.addDocument(externalId, cmd);
    }

    @Override
    public void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider) {
        log.info("Adicionando requisito ao envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.addRequirement(externalId, cmd);
    }

    @Override
    public void activateEnvelope(String externalId, ProviderSignature provider) {
        log.info("Ativando envelope {} no provedor {}", externalId, provider);
        ESignatureGateway gateway = registry.get(provider);
        gateway.activateEnvelope(externalId);
    }
}
