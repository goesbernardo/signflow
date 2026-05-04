package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;

@Builder
@Jacksonized
public record ClickSignResponseAttributesDTO(
    String name,
    String status,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    OffsetDateTime created,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]")
    OffsetDateTime modified,
    @JsonProperty("birthday")
    String birthday,
    @JsonProperty("phone_number")
    String phoneNumber,
    @JsonProperty("email")
    String email,
    @JsonProperty("has_documentation")
    Boolean hasDocumentation,
    @JsonProperty("documentation")
    String documentation,
    @JsonProperty("deadline_at")
    String deadline,
    String locale,
    @JsonProperty("auto_close")
    Boolean autoClose,
    @JsonProperty("remind_interval")
    Integer remindInterval,
    @JsonProperty("sequence_enabled")
    Boolean sequenceEnabled
) {}
