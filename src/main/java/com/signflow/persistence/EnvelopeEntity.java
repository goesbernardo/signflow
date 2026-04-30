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
    private UUID userId;
    @Enumerated(EnumType.STRING)
    private ProviderSignature provider;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime created;
    @Column(name = "external_id",unique = true)
    private String externalId;
}
