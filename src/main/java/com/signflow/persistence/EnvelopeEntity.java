package com.signflow.persistence;

import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ENVELOPE_REQUEST")
@Getter
@Setter
@NoArgsConstructor
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
