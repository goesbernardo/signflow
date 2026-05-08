package com.signflow.enums;

/**
 * Papeis (qualificações) do signatario no domínio SignFlow.
 * <p>
 * Cada gateway mapeia para o modelo especifico do provider.
 * <p>
 * ClickSign (requisito de qualificação — action: "agree"):
 *   SIGN ≥ role: "sign"
 *   PARTY ≥ role: "party"
 *   CONTRACTOR ≥ role: "contractor"
 *   WITNESS ≥ role: "witness"
 *   INTERVENING ≥ role: "intervening"
 * <p>
 * DocuSign (futuro):
 *   SIGN ≥ recipientType: "signer"
 *   WITNESS ≥ recipientType: "witness",
 *   etc.
 */
public enum SignerRole {

    /** Assinar — papel padrão */
    SIGN,

    /** Parte do contrato */
    PARTY,

    /** Contratante */
    CONTRACTOR,

    /** Testemunha */
    WITNESS,

    /** Interveniente */
    INTERVENING
}
