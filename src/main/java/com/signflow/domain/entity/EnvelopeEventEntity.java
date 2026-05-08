package com.signflow.domain.entity;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "envelope_event_seq")
    @SequenceGenerator(name = "envelope_event_seq", sequenceName = "envelope_event_seq", allocationSize = 50)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envelope_id", nullable = false)
    private EnvelopeEntity envelope;

    /**
     * Signatário que gerou o evento.
     * Preenchido nos eventos: sign, refusal, add_signer, remove_signer.
     * Null nos demais eventos (close, cancel, deadline, etc.).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id")
    private SignerEntity signer;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private Status previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private Status newStatus;

    /**
     * Status exato retornado pelo provedor.
     * Ex: "running", "completed", "canceled", "draft"
     */
    @Column(name = "provider_status")
    private String providerStatus;

    /**
     * Evento exato da ClickSign que gerou esta entrada.
     * Ex: "sign", "cancel", "close", "auto_close", "deadline", "refusal"
     */
    @Column(name = "provider_event")
    private String providerEvent;

    /**
     * Origem da mudança: "API" ou "WEBHOOK"
     */
    private String source;

    private LocalDateTime occurredAt;
}
