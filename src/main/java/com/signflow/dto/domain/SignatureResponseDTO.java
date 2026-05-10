package com.signflow.dto.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignatureResponseDTO {
    private String id;
    private String status;
    private OffsetDateTime created;
    private OffsetDateTime modified;
}
