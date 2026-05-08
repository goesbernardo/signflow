package com.signflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequirementAuth {

    // ── Token ─────────────────────────────────────────────────────────────────

    /**
     * Token enviado por e-mail.
     * Uso: contratos simples, baixo custo, sem restrições no sandbox.
     */
    EMAIL("email"),

    /**
     * Token enviado por SMS.
     * Uso: requer campo phone_number no signatário.
     * Nota: delivery deve ser "sms"; documentSigned não aceita "sms", usar fallback "email".
     */
    SMS("sms"),

    /**
     * Token enviado por WhatsApp.
     * Uso: requer canal WhatsApp Business configurado na conta ClickSign.
     */
    WHATSAPP("whatsapp"),

    // ── Pix ───────────────────────────────────────────────────────────────────

    /**
     * Autenticação via transação Pix — substitui o token.
     * Restrição: CPF do signatário é OBRIGATÓRIO (documentation + hasDocumentation: true).
     * Uso: contratos que exigem confirmação de identidade via chave Pix.
     */
    PIX("pix"),

    // ── Assinatura ────────────────────────────────────────────────────────────

    /**
     * Assinatura manuscrita digital.
     * Uso: signatário desenha a assinatura com mouse ou tela touch no navegador.
     */
    HANDWRITTEN("handwritten"),

    /**
     * Assinatura automática quando o grupo corrente do envelope for o mesmo do signatário.
     * Uso: fluxos automatizados sem interação humana do signatário.
     */
    AUTO_SIGNATURE("auto_signature"),

    /**
     * Assinatura presencial — signatário e anfitrião no mesmo local físico.
     * Uso: processos presenciais onde o anfitrião recebe o token/QR Code.
     */
    PRESENTIAL("presential"),

    // ── Imagem / Coleta de evidências ─────────────────────────────────────────

    /**
     * Selfie do signatário via câmera do dispositivo.
     * Uso: confirmação de identidade por foto. Incompatível com: facial_biometrics, liveness.
     */
    SELFIE("selfie"),

    /**
     * Selfie dinâmica (liveness detection).
     * Uso: prova de vida — detecta se é uma pessoa real e não uma foto.
     * Incompatível com: facial_biometrics.
     */
    LIVENESS("liveness"),

    /**
     * Biometria facial com reconhecimento por IA.
     * Custo: R$ 1,50 por biometria realizada com sucesso.
     * Incompatível com: selfie, liveness, official_document.
     */
    FACIAL_BIOMETRICS("facial_biometrics"),

    /**
     * Biometria SERPRO — consulta na base de dados governamental.
     * Custo: R$ 4,50 por consulta.
     * Restrição: CPF obrigatório. Incompatível com: selfie, liveness, official_document.
     */
    BIOMETRIC("biometric"),

    /**
     * Foto do documento oficial (RG, CNH) — frente e verso.
     * Uso: validação de identidade por documento físico.
     * Incompatível com: facial_biometrics, biometric.
     */
    OFFICIAL_DOCUMENT("official_document"),

    /**
     * Comprovante de endereço como autenticação.
     * Uso: contratos que exigem comprovação de residência.
     */
    ADDRESS_PROOF("address_proof"),

    /**
     * Documentoscopia — valida integridade do documento e extrai dados via OCR.
     * Custo: R$ 4,50 por solicitação.
     * Uso: validação de autenticidade do documento físico.
     */
    DOCUMENTSCOPY("documentscopy"),

    // ── Certificado Digital ───────────────────────────────────────────────────

    /**
     * Certificado digital ICP-Brasil (A1 ou A3).
     * Uso: documentos que exigem assinatura digital com validade jurídica máxima.
     * Nota: requer que o signatário possua certificado digital instalado.
     */
    ICP_BRASIL("icp_brasil"),

    // ── Widget Embedded ───────────────────────────────────────────────────────

    /**
     * Assinatura via widget embedded sem token de autenticação.
     * Uso: assinatura integrada à aplicação externa — ambiente controlado ou presencial.
     * Requer: nome, CPF e data de nascimento do signatário.
     */
    EMBEDDED_SIGNATURE("embedded_signature"),

    // ── API (uso programático) ────────────────────────────────────────────────

    /**
     * Assinatura programática — não requer interação humana.
     * Uso: sistemas automatizados onde a assinatura é feita via API.
     */
    API("api");

    @JsonValue
    private final String value;

    @JsonCreator
    public static RequirementAuth fromValue(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        for (RequirementAuth auth : values()) {
            if (auth.value.equalsIgnoreCase(value) || auth.name().equalsIgnoreCase(value)) {
                return auth;
            }
        }
        throw new IllegalArgumentException(
                "RequirementAuth inválido: " + value + ". Valores aceitos: " +
                        java.util.Arrays.stream(values())
                                .map(RequirementAuth::getValue)
                                .collect(java.util.stream.Collectors.joining(", "))
        );
    }
}
