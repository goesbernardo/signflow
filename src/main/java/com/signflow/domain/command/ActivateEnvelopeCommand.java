package com.signflow.domain.command;

import com.signflow.enums.SignatureAuthMethod;
import com.signflow.enums.SignerRole;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Command para ativação completa do envelope.
 * <p>
 * O endpoint POST /activate usa este command para criar
 * automaticamente os dois requisitos obrigatórios da ClickSign
 * (qualificação + autenticacao) e ativar o envelope.
 */

@Builder
@Jacksonized
public record ActivateEnvelopeCommand(

        String signerId,

        String documentId,

        // Papel do signatario.
        // Default: SIGN
        SignerRole role,

         // Default: EMAIL
        SignatureAuthMethod auth
) {}

