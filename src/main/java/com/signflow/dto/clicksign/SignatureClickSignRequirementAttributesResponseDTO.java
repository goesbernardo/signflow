package com.signflow.dto.clicksign;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class SignatureClickSignRequirementAttributesResponseDTO {

    private String action;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
