package com.signflow.infrastructure.persistence.entity;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    public EnvelopeEntity(Long id, String userId, String name, ProviderSignature provider, Status status, String providerStatus, LocalDateTime created, String externalId, List<SignerEntity> signers, List<DocumentEntity> documents, List<EnvelopeEventEntity> events) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.provider = provider;
        this.status = status;
        this.providerStatus = providerStatus;
        this.created = created;
        this.externalId = externalId;
        this.signers = signers;
        this.documents = documents;
        this.events = events;
    }
}
