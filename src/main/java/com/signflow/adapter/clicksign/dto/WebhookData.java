package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookData {

    @JsonProperty("id")
    private String id;              // externalId do envelope

    @JsonProperty("type")
    private String type;            // "envelopes"

    @JsonProperty("attributes")
    private WebhookAttributesDTO attributes;


}
