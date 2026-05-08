package com.signflow.infrastructure.provider.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClicksignActivateAttributesDTO(
    String status,
    String name,
    @JsonProperty("deadline_at")
    String deadlineAt,
    String locale,
    @JsonProperty("auto_close")
    Boolean autoClose,
    @JsonProperty("default_message")
    String defaultMessage
) {}
