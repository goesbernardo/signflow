package com.signflow.application.port.in;

import com.signflow.api.dto.EnvelopeTimelineResponse;
import com.signflow.domain.command.*;
import com.signflow.domain.model.Document;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Contrato de negócio para operações de assinatura eletrônica no SignFlow.
 * <p>
 * Orquestra as chamadas ao gateway do provider selecionado e mantém
 * o estado local no banco de dados do SignFlow.
 * <p>
 * Os métodos marcados com TODO serão implementados quando os respectivos
 * gateways de provider suportarem as operações correspondentes.
 */

public interface SignatureService {
    // ── Envelope ──────────────────────────────────────────────────────────

    /**
     * Cria um novo envelope no provider e persiste localmente.
     */
    Envelope createEnvelope(CreateEnvelopeCommand cmd, ProviderSignature provider);

    /**
     * Atualiza os dados de um envelope existente (somente antes da ativação).
     */
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd, ProviderSignature provider);

    /**
     * Consulta o envelope — primeiro no banco local, depois no provider.
     */
    Envelope getEnvelope(String externalId, ProviderSignature provider);

    /**
     * Ativa o envelope e dispara as notificações para os signatários.
     */
    void activateEnvelope(String externalId, ProviderSignature provider);

    /**
     * Cria envelope completo em uma única chamada:
     * envelope → documentos → signatários → requisitos → ativação opcional.
     */
    Envelope createFullEnvelope(CreateFullEnvelopeCommand cmd, ProviderSignature provider);

    /**
     * Lista envelopes do usuário autenticado com paginação e filtro opcional de status.
     */
    Page<Envelope> listEnvelopes(Status status, Pageable pageable);

    /**
     * Retorna a timeline de eventos de auditoria do envelope.
     */
    List<EnvelopeTimelineResponse> getTimeline(String externalId);

    // ── Documento ─────────────────────────────────────────────────────────

    /**
     * Adiciona um documento ao envelope via upload em base64.
     */
    Document addDocument(String externalId, AddDocumentCommand cmd, ProviderSignature provider);

    /**
     * Lista os documentos do envelope.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    List<Document> getDocuments(String externalId, ProviderSignature provider);

    /**
     * Consulta um documento específico pelo seu externalId.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    Document getDocument(String documentId, ProviderSignature provider);

    /**
     * Atualiza os dados de um documento.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    Document updateDocument(String documentId, UpdateDocumentCommand cmd, ProviderSignature provider);

    /**
     * Remove um documento do envelope.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    void deleteDocument(String documentId, ProviderSignature provider);

    // ── Signatário ────────────────────────────────────────────────────────

    /**
     * Adiciona múltiplos signatários ao envelope.
     */
    List<Signer> addSigners(String externalId, List<AddSignerCommand> commands, ProviderSignature provider);

    /**
     * Lista os signatários do envelope.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    List<Signer> getSigners(String externalId, ProviderSignature provider);

    /**
     * Consulta um signatário específico do envelope.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    Signer getSigner(String externalId, String signerId, ProviderSignature provider);

    /**
     * Remove um signatário do envelope (somente antes da ativação).
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    void deleteSigner(String externalId, String signerId, ProviderSignature provider);

    // ── Requisito ─────────────────────────────────────────────────────────

    /**
     * Adiciona um requisito de assinatura ao envelope.
     * Tipos via enums do domínio: role (qualificação) ou auth (autenticação).
     */
    void addRequirement(String externalId, AddRequirementCommand cmd, ProviderSignature provider);

    /**
     * Lista os requisitos do envelope.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    List<Requirement> getRequirements(String externalId, ProviderSignature provider);

    /**
     * Consulta um requisito específico pelo seu externalId.
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    Requirement getRequirement(String requirementId, ProviderSignature provider);

    /**
     * Remove um requisito do envelope (somente antes da ativação).
     * TODO: implementar no ESignatureGateway e nos gateways de provider.
     */
    void deleteRequirement(String requirementId, ProviderSignature provider);

}
