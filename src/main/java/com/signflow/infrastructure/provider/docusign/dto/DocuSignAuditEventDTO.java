package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DocuSignAuditEventDTO(
        @JsonProperty("eventFields") List<DocuSignAuditFieldDTO> eventFields
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DocuSignAuditFieldDTO(@JsonProperty("name") String name, @JsonProperty("value") String value) {}
}
