package com.signflow.domain.model;

import com.signflow.enums.Status;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Builder
public class Envelope {
    private String id;
    private String externalId;
    private String name;
    private Status status;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
