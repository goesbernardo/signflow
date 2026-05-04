package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AddRequirementCommand(
        String signerId,
        String documentId,
        String action,          // "sign" | "agree"
        String auth,            // "email" | "sms" | "whatsapp" | "api"
        @JsonProperty("rubric_pages")
        String rubricPages,     // só quando action = "agree" — ex: "all" ou "1,2"
        String role             // só para qualificação — ex: "intervening"
) {}
