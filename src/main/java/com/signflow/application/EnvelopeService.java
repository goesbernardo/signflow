package com.signflow.application;

import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;

public interface EnvelopeService {
    Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider);
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider);
    Envelope getEnvelope(String externalId, ProviderSignature provider);
    Signer addSigner(String externalId, AddSignerCommand cmd, ProviderSignature provider);
    Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider);
    void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider);
    void activateEnvelope(String externalId, ProviderSignature provider);
}
