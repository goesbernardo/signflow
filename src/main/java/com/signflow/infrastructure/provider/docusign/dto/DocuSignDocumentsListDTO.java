package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignDocumentsListDTO(
        @JsonProperty("envelopeDocuments") List<DocuSignDocumentResponseDTO> envelopeDocuments
) {}
