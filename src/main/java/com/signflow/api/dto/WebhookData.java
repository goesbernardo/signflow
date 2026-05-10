package com.signflow.adapter.clicksign.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.enums.ProviderSignature;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public record WebhookData(
    ProviderSignature provider
) {}
