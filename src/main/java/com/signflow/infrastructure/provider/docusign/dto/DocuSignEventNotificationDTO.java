package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignEventNotificationDTO(
        @JsonProperty("url") String url,
        @JsonProperty("loggingEnabled") Boolean loggingEnabled,
        @JsonProperty("requireAcknowledgment") Boolean requireAcknowledgment,
        @JsonProperty("useSoapInterface") Boolean useSoapInterface,
        @JsonProperty("includeCertificateWithSoap") Boolean includeCertificateWithSoap,
        @JsonProperty("signMessageWithX509Cert") Boolean signMessageWithX509Cert,
        @JsonProperty("includeDocuments") Boolean includeDocuments,
        @JsonProperty("includeEnvelopeVoidReason") Boolean includeEnvelopeVoidReason,
        @JsonProperty("includeTimeZone") Boolean includeTimeZone,
        @JsonProperty("includeSenderAccountAsCustomField") Boolean includeSenderAccountAsCustomField,
        @JsonProperty("includeDocumentFields") Boolean includeDocumentFields,
        @JsonProperty("includeCertificateOfCompletion") Boolean includeCertificateOfCompletion,
        @JsonProperty("envelopeEvents") List<DocuSignEnvelopeEventDTO> envelopeEvents,
        @JsonProperty("recipientEvents") List<DocuSignRecipientEventDTO> recipientEvents
) {}
