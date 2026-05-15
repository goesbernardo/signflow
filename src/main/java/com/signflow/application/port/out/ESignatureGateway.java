package com.signflow.application.port.out;

import com.signflow.domain.command.*;
import com.signflow.domain.model.AuditEvent;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.EmbeddedSigningView;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import java.util.List;

/**
 * Porta de saída para serviços de assinatura eletrônica.
 * Define o contrato que os adaptadores (providers) devem implementar.
 */
public interface ESignatureGateway {
    // ── Operações de Envelope ─────────────────────────────────────────────

    Envelope createEnvelope(CreateEnvelopeCommand cmd);

    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd);

    Envelope getEnvelope(String externalId);

    void activateEnvelope(String envelopeId);

    void cancelEnvelope(String envelopeId);

    void remindSigner(String envelopeId, String signerId);

    default String addNotifier(String envelopeId, AddNotifierCommand cmd) {
        return null;
    }

    // ── Operações de Documento ────────────────────────────────────────────

    Document addDocument(String envelopeId, AddDocumentCommand cmd);

    default byte[] downloadDocument(String envelopeId, String documentId) {
        throw new UnsupportedOperationException("downloadDocument não suportado por " + provider());
    }

    default byte[] downloadCertificate(String envelopeId) {
        throw new UnsupportedOperationException("downloadCertificate não suportado por " + provider());
    }

    // ── Embedded Signing ──────────────────────────────────────────────────

    default EmbeddedSigningView createEmbeddedSigningView(String envelopeId, CreateEmbeddedSigningCommand cmd) {
        throw new UnsupportedOperationException("Embedded signing não suportado por " + provider());
    }

    // ── Audit Trail ───────────────────────────────────────────────────────

    default List<AuditEvent> getAuditEvents(String envelopeId) {
        return List.of();
    }

    // ── Operações de Signatário ───────────────────────────────────────────

    Signer addSigner(String envelopeId, AddSignerCommand cmd);

    default List<Signer> addSigners(String envelopeId, List<AddSignerCommand> commands, ProviderSignature provider) {
        return commands.stream()
                .map(cmd -> addSigner(envelopeId, cmd))
                .toList();
    }

    // ── Operações de Requisito ────────────────────────────────────────────

    Requirement addRequirement(String envelopeId, AddRequirementCommand cmd);

    // ── Identificação do provider ─────────────────────────────────────────

    ProviderSignature provider();
}
