package com.signflow.infrastructure.persistence.entity;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "envelope_request")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"signers", "documents", "events"})
public class EnvelopeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "user_id")
    private String userId;
    private String name;
    @Enumerated(EnumType.STRING)
    private ProviderSignature provider;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column(name = "provider_status")
    private String providerStatus;
    private LocalDateTime created;
    @Column(name = "external_id",unique = true)
    private String externalId;

    @OneToMany(mappedBy = "envelope", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SignerEntity> signers;

    @OneToMany(mappedBy = "envelope", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DocumentEntity> documents;

    @OneToMany(mappedBy = "envelope", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<EnvelopeEventEntity> events;
}
