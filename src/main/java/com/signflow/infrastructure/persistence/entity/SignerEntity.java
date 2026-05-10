package com.signflow.infrastructure.persistence.entity;

import com.signflow.infrastructure.security.EncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "signer")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = "envelope")
public class SignerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "signer_seq")
    @SequenceGenerator(name = "signer_seq", sequenceName = "signer_seq", allocationSize = 50)
    private Long id;

    @Column(name = "external_id", unique = true)
    private String externalId;

    @Convert(converter = EncryptionConverter.class)
    private String name;

    @Convert(converter = EncryptionConverter.class)
    private String email;

    @Convert(converter = EncryptionConverter.class)
    private String documentation;

    @Column(name = "auth_method")
    private String authMethod;

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

    @Column(name = "last_reminded_at")
    private LocalDateTime lastRemindedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    private EnvelopeEntity envelope;

    private LocalDateTime created;
}
