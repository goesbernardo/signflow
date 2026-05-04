package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignRequirementsAttributesDTO(
        String action,
        String auth,
        @JsonProperty("rubric_pages")
        String rubricPages,    // ← "all" ou "1,2,3" — só para action: agree
        String role            // ← "intervening", "witness", etc — para qualificação
) {}
