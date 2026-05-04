package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClicksignActivateAttributesDTO(
    String status,
    String name,
    @JsonProperty("deadline_at")
    String deadlineAt,
    String locale,
    @JsonProperty("auto_close")
    boolean autoClose,
    @JsonProperty("default_message")
    String defaultMessage
) {}
