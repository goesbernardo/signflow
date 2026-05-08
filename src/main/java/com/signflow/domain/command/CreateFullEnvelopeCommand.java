package com.signflow.domain.command;

import jakarta.validation.Valid;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Builder
@Jacksonized
public record CreateFullEnvelopeCommand(
        String name,
        @Valid
        List<AddDocumentCommand> documents,
        @Valid
        List<AddSignerCommand> signers,
        @Valid
        List<FullRequirementCommand> requirements, // ← sem documentId/signerId
        Boolean autoActivate
) {}
