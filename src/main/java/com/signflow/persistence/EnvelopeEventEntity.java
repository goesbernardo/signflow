package com.signflow.persistence;

import com.signflow.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "envelope_event")
@Getter
@Setter
@NoArgsConstructor
public class EnvelopeEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    private EnvelopeEntity envelope;

    @Enumerated(EnumType.STRING)
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    private Status newStatus;

    private String source;

    private LocalDateTime occurredAt;
}
