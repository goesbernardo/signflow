package com.signflow.domain.command;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ActivateEnvelopeCommand(

        String signerId,        // externalId do signatário

        String documentId,      // externalId do documento

        String role,            // role do signatário: "sign" | "party" | "contractor"
        // default: "sign"

        String auth             // método de autenticação: "email" | "sms" | "whatsapp"
        // default: "email"
) {}

