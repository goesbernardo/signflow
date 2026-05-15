package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignTabDTO(
        @JsonProperty("signHereTabs") List<DocuSignSignHereTabDTO> signHereTabs,
        @JsonProperty("initialHereTabs") List<DocuSignSignHereTabDTO> initialHereTabs,
        @JsonProperty("dateSignedTabs") List<DocuSignSignHereTabDTO> dateSignedTabs
) {}
