package com.signflow.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Document {
    private String externalId;
    private String filename;
}
