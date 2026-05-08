package com.signflow.enums;
/**
        * Métodos de autenticacao suportados pelo domínio SignFlow.
 * <p>
         * Cada gateway e responsável por mapear esses valores
         * para os campos específicos do seu provider.
 * <p>
        * ClickSign:
        *   EMAIL -> auth: "email"
        *   SMS -> auth: "sms"
        *   WHATSAPP -> auth: "whatsapp"
        *   PIX -> auth: "pix"
        *   HANDWRITTEN -> auth: "handwritten"
        *   FACIAL_BIOMETRICS -> auth: "facial_biometrics"
        *   API -> auth: "api"
        *   AUTO -> auth: "auto_signature"
 * <p>
        * DocuSign (futuro):
        *   EMAIL             -> recipientType: "signer", deliveryMethod: "email"
        *   SMS               -> recipientType: "signer", smsAuthentication: { ... }
        *   etc.
 */
public enum SignatureAuthMethod {

    /** Autenticacao via link enviado por e-mail */
    EMAIL,

    /** Autenticacao via token enviado por SMS */
    SMS,

    /** Autenticacao via token enviado por WhatsApp */
    WHATSAPP,

    /** Autenticacao via pagamento Pix de R$ 0,01 com validação de CPF */
    PIX,

    /** Assinatura manuscrita desenhada via canvas no navegador */
    HANDWRITTEN,

    /** Reconhecimento facial via camera — exige documentation (CPF) */
    FACIAL_BIOMETRICS,

    /** Assinatura programática sem interação humana */
    API,

    /** Assinatura automática — exige Termo de Assinatura Automática prévio */
    AUTO
}
