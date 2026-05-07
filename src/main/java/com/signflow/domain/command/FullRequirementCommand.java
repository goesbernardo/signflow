package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.enums.RequirementAuth;
import com.signflow.enums.RequirementRole;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FullRequirementCommand(
        /**
         * Obrigatório quando action = PROVIDE_EVIDENCE
         * Ex: EMAIL, SMS, WHATSAPP
         */
        RequirementAuth auth,

        /**
         * Obrigatório quando action = AGREE
         * Ex: SIGN, WITNESS, INTERVENING
         */
        RequirementRole role,

        /**
         * Opcional — páginas para rubrica quando action = AGREE
         * Ex: "all" ou "1,2,3"
         */
        @JsonProperty("rubric_pages")
        String rubricPages
) {}
