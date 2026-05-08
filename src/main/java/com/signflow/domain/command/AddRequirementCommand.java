package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.enums.*;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Command para adicionar um requisito a um envelope.
 * <p>
 * A ClickSign exige dois requisitos por signatario:
 *  1. Qualificação: role (define o papel — SignerRole)
 *  2. Autenticacao: auth (define como autentica — SignatureAuthMethod)
 * <p>
 * O endpoint POST /api/v1/signatures/{id}/activate cria os dois
 * automaticamente usando os defaults (SIGN + EMAIL) quando não informados.
 * <p>
 * rubricPages era um campo específico da ClickSign e foi removido do domínio.
 * O gateway ClickSign usa "rubricate" como action e "all" como paginas
 * quando o requisito de rubrica for necessário.
 */

@Builder
@Jacksonized
public record AddRequirementCommand(
        String signerId,

        String documentId,


         // Método de autenticacao do signatario.
         // Mapeado para cada provider pelo respectivo gateway.
         // Default: EMAIL

        SignatureAuthMethod auth,


         // Papel (qualificação) do signatario.
         // Mapeado para cada provider pelo respectivo gateway.
         // Default: SIGN

        SignerRole role
) {}
