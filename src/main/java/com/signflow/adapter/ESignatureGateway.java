package com.signflow.adapter;

import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.UpdateDocumentCommand;
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
    // ── Operações de Envelope ─────────────────────────────────────────────

    /**
     * Cria um novo envelope no provider.
     *
     * @param cmd dados do envelope (nome, deadline, locale, etc.)
     * @return envelope criado com externalId do provider
     */
    Envelope createEnvelope(CreateEnvelopeCommand cmd);

    /**
     * Atualiza os dados de um envelope existente.
     * Só é possível antes da ativação (status DRAFT).
     *
     * @param externalId ID do envelope no provider
     * @param cmd dados a atualizar
     * @return envelope atualizado
     */
    Envelope updateEnvelope(String externalId, UpdateEnvelopeCommand cmd);

    /**
     * Consulta o estado atual de um envelope no provider.
     *
     * @param externalId ID do envelope no provider
     * @return envelope com status atualizado
     */
    Envelope getEnvelope(String externalId);

    /**
     * Ativa o envelope — dispara as notificações para os signatários.
     * Após a ativação o envelope não pode mais ser editado.
     * Todos os requisitos obrigatórios devem ter sido criados antes.
     *
     * @param envelopeId ID do envelope no provider
     */
    void activateEnvelope(String envelopeId);

    /**
     * Cancela um envelope ativo ou em rascunho.
     * Operação irreversível — o envelope não pode ser reativado.
     *
     * @param envelopeId ID do envelope no provider
     *
     * TODO: Melhoria #06 — implementar em todos os gateways
     */
    default void cancelEnvelope(String envelopeId) {
        throw new UnsupportedOperationException("cancelEnvelope não implementado para o provider: " + provider().name());
    }

    // ── Operações de Documento ────────────────────────────────────────────

    /**
     * Adiciona um documento ao envelope via upload em base64.
     *
     * @param envelopeId ID do envelope no provider
     * @param cmd dados do documento (filename, contentBase64)
     * @return documento criado com externalId do provider
     */
    Document addDocument(String envelopeId, AddDocumentCommand cmd);

    // ── Operações de Signatário ───────────────────────────────────────────

    /**
     * Adiciona um signatário ao envelope.
     * O canal de notificação (EMAIL, SMS, WHATSAPP) é definido no command
     * via o enum NotificationChannel — neutro entre providers.
     *
     * @param envelopeId ID do envelope no provider
     * @param cmd dados do signatário (nome, email, canal de notificação, etc.)
     * @return signatário criado com externalId do provider
     */
    Signer addSigner(String envelopeId, AddSignerCommand cmd);

    /**
     * Adiciona múltiplos signatários ao envelope de uma vez.
     *
     * @param envelopeId ID do envelope no provider
     * @param commands   lista de dados dos signatários
     * @param provider   o provider utilizado (para compatibilidade de interface)
     * @return lista de signatários criados
     */
    default List<Signer> addSigners(String envelopeId, List<AddSignerCommand> commands, ProviderSignature provider) {
        return commands.stream()
                .map(cmd -> addSigner(envelopeId, cmd))
                .toList();
    }

    /**
     * Adiciona um requisito de assinatura ao envelope.
     *
     * Tipos de requisito definidos via enums do domínio:
     *  - role != null  → requisito de qualificação (papel do signatário)
     *  - auth != null  → requisito de autenticação (como o signatário se autentica)
     *
     * Cada gateway interpreta esses enums e cria o requisito no formato do seu provider.
     *
     * @param envelopeId ID do envelope no provider
     * @param cmd dados do requisito (signerId, documentId, role, auth)
     * @return requisito criado com externalId do provider
     */
    Requirement addRequirement(String envelopeId, AddRequirementCommand cmd);

//    /**
//     * Envia um lembrete manual para um signatário pendente.
//     *
//     * @param envelopeId ID do envelope no provider
//     * @param signerId   ID do signatário no provider
//     *
//     * TODO: Melhoria #07 — implementar em todos os gateways
//     */
//    default void remindSigner(String envelopeId, String signerId) {
//        throw new UnsupportedOperationException("remindSigner não implementado para o provider: " + provider().name());
//    }


    // ── Identificação do provider ─────────────────────────────────────────

    /**
     * Retorna o identificador do provider implementado por este gateway.
     * Usado pelo GatewayRegistry para selecionar o gateway correto.
     *
     * @return enum ProviderSignature deste gateway
     */
    ProviderSignature provider();
}
