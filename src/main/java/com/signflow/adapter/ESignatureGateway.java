package com.signflow.adapter;

import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.command.UpdateEnvelopeCommand;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;

public interface ESignatureGateway {
    Envelope createEnvelope(CreateEnvelopeCommand cmd);
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd);
    Envelope getEnvelope(String externalId);
    Signer addSigner(String envelopeId, AddSignerCommand cmd);
    Document addDocument(String envelopeId, AddDocumentCommand cmd);
    void addRequirement(String envelopeId, AddRequirementCommand cmd);
    void activateEnvelope(String envelopeId);
    ProviderSignature provider();
}
