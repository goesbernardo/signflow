package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ClickSignCreateSignEventsDTO {

    @JsonProperty("signature_request")
    private String signatureRequest;
    @JsonProperty("signature_reminder")
    private String signatureReminder;
    @JsonProperty("document_signed")
    private String documentSigned;

}
