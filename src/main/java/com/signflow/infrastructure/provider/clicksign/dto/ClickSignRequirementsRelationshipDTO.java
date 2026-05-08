package com.signflow.infrastructure.provider.clicksign.dto;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Builder
@Jacksonized
public record ClickSignRequirementsRelationshipDTO(
    RelationshipDataDTO document,
    RelationshipDataDTO signer
) {}
