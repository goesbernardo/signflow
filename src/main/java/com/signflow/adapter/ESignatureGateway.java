package com.signflow.adapter;

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
import java.util.List;

public interface ESignatureGateway {
    Envelope createEnvelope(CreateEnvelopeCommand cmd);
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd);
    Envelope getEnvelope(String externalId);
    List<Signer> addSigners(String envelopeId, List<AddSignerCommand> commands);
    Document addDocument(String envelopeId, AddDocumentCommand cmd);
    Requirement addRequirement(String envelopeId, AddRequirementCommand cmd);
    void activateEnvelope(String envelopeId);
    ProviderSignature provider();
}
