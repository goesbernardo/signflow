package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignSignerDTO(
        @JsonProperty("email") String email,
        @JsonProperty("name") String name,
        @JsonProperty("recipientId") String recipientId,
        @JsonProperty("routingOrder") String routingOrder,
        @JsonProperty("deliveryMethod") String deliveryMethod,
        @JsonProperty("phoneAuthentication") DocuSignPhoneAuthDTO phoneAuthentication
) {}
