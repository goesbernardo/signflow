package com.signflow.domain.model;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Document {
    private String externalId;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
