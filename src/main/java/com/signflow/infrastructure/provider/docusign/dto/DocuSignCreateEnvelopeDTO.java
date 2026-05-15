package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignCreateEnvelopeDTO(
        @JsonProperty("emailSubject") String emailSubject,
        @JsonProperty("status") String status,
        @JsonProperty("documents") List<DocuSignDocumentDTO> documents,
        @JsonProperty("recipients") DocuSignRecipientsDTO recipients,
        @JsonProperty("eventNotification") DocuSignEventNotificationDTO eventNotification
) {}
