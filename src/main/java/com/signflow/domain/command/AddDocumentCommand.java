package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record AddDocumentCommand(
    String filename,
    @JsonProperty("content_base64")
    String contentBase64,
    @Email
    String email
) {}
