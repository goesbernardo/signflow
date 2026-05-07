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
    List<Signer> addSigners(String externalId, List<AddSignerCommand> commands, ProviderSignature provider);
    List<Signer> getSigners(String externalId, ProviderSignature provider);
    Signer getSigner(String externalId, String signerId, ProviderSignature provider);
    void deleteSigner(String externalId, String signerId, ProviderSignature provider);
    Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider);
    List<Document> getDocuments(String externalId, ProviderSignature provider);
    Document getDocument(String documentId, ProviderSignature provider);
    Document updateDocument(String documentId, UpdateDocumentCommand cmd, ProviderSignature provider);
    void deleteDocument(String documentId, ProviderSignature provider);
    void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider);
    List<Requirement> getRequirements(String externalId, ProviderSignature provider);
    Requirement getRequirement(String requirementId, ProviderSignature provider);
    void deleteRequirement(String requirementId, ProviderSignature provider);
    void activateEnvelope(String externalId, ProviderSignature provider);
    List<EnvelopeTimelineResponse> getTimeline(String externalId);
    Page<Envelope> listEnvelopes(Status status, Pageable pageable);
    Envelope createFullEnvelope(CreateFullEnvelopeCommand cmd, ProviderSignature provider);

}
