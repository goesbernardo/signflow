package com.signflow.application;

import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.api.dto.EnvelopeTimelineResponse;

import com.signflow.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EnvelopeService {
    Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider);
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider);
    Envelope getEnvelope(String externalId, ProviderSignature provider);
    Signer addSigner(String externalId, AddSignerCommand cmd, ProviderSignature provider);
    Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider);
    Requirement addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider);
    void activateEnvelope(String externalId, ProviderSignature provider);
    List<EnvelopeTimelineResponse> getTimeline(String externalId);
    Page<Envelope> listEnvelopes(Status status, Pageable pageable);
    void activateEnvelopeComplete(ProviderSignature provider, String externalId, ActivateEnvelopeCommand command);

}
