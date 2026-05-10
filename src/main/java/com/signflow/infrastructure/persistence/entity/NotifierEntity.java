package com.signflow.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "envelope_notifier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    private EnvelopeEntity envelope;

    @Column(nullable = false)
    private String email;

    private String name;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
