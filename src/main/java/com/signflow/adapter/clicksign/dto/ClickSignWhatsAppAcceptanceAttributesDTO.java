package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClickSignWhatsAppAcceptanceAttributesDTO(
        String title,

        @JsonProperty("sender_name_option")
        String senderNameOption,

        @JsonProperty("sender_phone")
        String senderPhone,

        String message,

        @JsonProperty("signer_phone")
        String signerPhone,

        @JsonProperty("signer_name")
        String signerName
) {
}
