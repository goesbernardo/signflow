package com.signflow.domain.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddDocumentCommand {
    private String filename;
    @JsonProperty("content_base64")
    private String contentBase64;
    @Email
    private String email;
}
