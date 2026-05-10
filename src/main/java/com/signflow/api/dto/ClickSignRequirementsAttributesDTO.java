package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.enums.RequirementAction;
import com.signflow.enums.RequirementAuth;
import com.signflow.enums.RequirementRole;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignRequirementsAttributesDTO(
        // ── Campos base ──────────────────────────────────────────────────────

        RequirementAction action,

        /**
         * Método de autenticação — só presente quando action = PROVIDE_EVIDENCE.
         * Valores: email | sms | whatsapp | pix | handwritten | selfie | liveness |
         *          facial_biometrics | biometric | official_document | address_proof |
         *          documentscopy | icp_brasil | embedded_signature | auto_signature |
         *          presential | api
         */
        RequirementAuth auth,

        /**
         * Papel do signatário — só presente quando action = AGREE.
         * Ex: sign, witness, intervening, guarantor, etc.
         */
        RequirementRole role,

        /**
         * Páginas para rubrica — só quando action = AGREE e com rubrica.
         * Valores: "all" ou "1,2,3"
         */
        @JsonProperty("rubric_pages")
        String rubricPages,

        // ── Campos opcionais por tipo de auth ────────────────────────────────

        /**
         * Habilita coleta de selfie — usado com auth: selfie ou liveness.
         * Não permitido quando auth = facial_biometrics ou biometric.
         */
        @JsonProperty("selfie_enabled")
        Boolean selfieEnabled,

        /**
         * Habilita liveness detection — usado com auth: liveness.
         * Não permitido quando auth = facial_biometrics ou biometric.
         */
        @JsonProperty("liveness_enabled")
        Boolean livenessEnabled,

        /**
         * Habilita documento oficial — usado com auth: official_document.
         * Não permitido quando auth = facial_biometrics ou biometric.
         */
        @JsonProperty("official_document_enabled")
        Boolean officialDocumentEnabled,

        /**
         * Habilita comprovante de endereço — usado com auth: address_proof.
         */
        @JsonProperty("address_proof_enabled")
        Boolean addressProofEnabled,

        /**
         * Habilita documentoscopia — usado com auth: documentscopy.
         */
        @JsonProperty("documentscopy_enabled")
        Boolean documentscopyEnabled
) {}
