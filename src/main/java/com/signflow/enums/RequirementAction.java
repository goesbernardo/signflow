package com.signflow.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RequirementAction {

    AGREE("agree"),
    PROVIDE_EVIDENCE("provide_evidence"),
    RUBRICATE("rubricate");

    @JsonValue
    private final String value;

    @JsonCreator
    public static RequirementAction fromValue(String value) {
        if (value == null || value.isBlank()) return null;
        for (RequirementAction action : values()) {
            if (action.value.equalsIgnoreCase(value) || action.name().equalsIgnoreCase(value)) {
                return action;
            }
        }
        throw new IllegalArgumentException("RequirementAction inválido: " + value + ". Valores aceitos: agree, provide_evidence, rubricate");
    }
}
