package com.signflow.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "signer")
@Getter
@Setter
@NoArgsConstructor
public class SignerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signer_seq")
    @SequenceGenerator(name = "signer_seq", sequenceName = "signer_seq", allocationSize = 50)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    private String name;

    private String email;

    /**
     * Status do signatário em relação à assinatura.
     * PENDING  → ainda não assinou
     * SIGNED   → assinou (webhook: sign)
     * REFUSED  → recusou (webhook: refusal)
     */
    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    /**
     * Momento exato da assinatura — preenchido via webhook "sign".
     */
    @Column(name = "signed_at")
    private LocalDateTime signedAt;

    /**
     * IP do signatário no momento da assinatura — preenchido via webhook "sign".
     */
    @Column(name = "ip_address")
    private String ipAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    private EnvelopeEntity envelope;

    private LocalDateTime created;
}
