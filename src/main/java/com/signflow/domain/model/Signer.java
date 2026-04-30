package com.signflow.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Signer {
    private String externalId;
    private String name;
    private String email;
}
