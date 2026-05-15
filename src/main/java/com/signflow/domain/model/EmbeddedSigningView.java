package com.signflow.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmbeddedSigningView {
    private String signingUrl;
    private String envelopeId;
    private String recipientEmail;
    private LocalDateTime expiresAt;
}
