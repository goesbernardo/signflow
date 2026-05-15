package com.signflow.infrastructure.provider.docusign.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocuSignRecipientViewRequestDTO(
        @JsonProperty("authenticationMethod") String authenticationMethod,
        @JsonProperty("clientUserId") String clientUserId,
        @JsonProperty("email") String email,
        @JsonProperty("returnUrl") String returnUrl,
        @JsonProperty("userName") String userName,
        @JsonProperty("recipientId") String recipientId,
        @JsonProperty("pingFrequency") String pingFrequency,
        @JsonProperty("pingUrl") String pingUrl
) {}
