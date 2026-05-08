package com.signflow.domain.model;

import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Signer {
    private String externalId;
    private String name;
    private String email;
    private String status;
    private OffsetDateTime signedAt;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
