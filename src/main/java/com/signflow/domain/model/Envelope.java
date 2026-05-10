package com.signflow.domain.model;

import com.signflow.enums.Status;
import lombok.Builder;
import lombok.Data;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class Envelope {
    private String id;
    private String externalId;
    private String name;
    private Status status;
    private OffsetDateTime created;
    private List<Signer> signers;
    private String provider;
    private String callbackUrl;
}
