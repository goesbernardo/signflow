package com.signflow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class Requirement {
    private String externalId;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
