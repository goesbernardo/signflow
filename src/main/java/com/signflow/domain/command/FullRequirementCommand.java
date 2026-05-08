package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.enums.RequirementAuth;
import com.signflow.enums.RequirementRole;
import com.signflow.enums.SignatureAuthMethod;
import com.signflow.enums.SignerRole;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

/**
 * Command para definir os requisitos de assinatura no fluxo completo.
 * <p>
 * Usado exclusivamente pelo endpoint POST /create-activate-envelope.
 * <p>
 * O mapeamento para os tipos internos de cada provider é feito
 * exclusivamente no gateway correspondente (ClickSignGateway,
 * D4SignGateway, etc.) — o domínio não conhece os detalhes de nenhum.
 * <p>
 * Exemplos de body:
 *   {"auth": "EMAIL", "role": "SIGN" } // e-mail padrão
 *   {"auth": "PIX", "role": "SIGN" } // pix
 *   {"auth": "SMS", "role": "CONTRACTOR" } // sms + contratante
 *   {"auth": "FACIAL_BIOMETRICS", "role": "SIGN" }
 */

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FullRequirementCommand(
        /*
          Método de autenticação do signatário.
          Valores: EMAIL | SMS | WHATSAPP | PIX | HANDWRITTEN | FACIAL_BIOMETRICS | API | AUTO
          Default aplicado no service: EMAIL
         */
        SignatureAuthMethod auth,

        /*
          Papel (qualificação) do signatário no contrato.
          Valores: SIGN | PARTY | CONTRACTOR | WITNESS | INTERVENING
          Default aplicado no service: SIGN
         */
        SignerRole role
) {}
