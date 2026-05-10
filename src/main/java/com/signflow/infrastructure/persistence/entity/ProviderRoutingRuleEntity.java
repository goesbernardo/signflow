package com.signflow.infrastructure.persistence.entity;

import com.signflow.enums.ProviderSignature;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider_routing_rule")
@Getter
@Setter
@NoArgsConstructor
public class ProviderRoutingRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private Integer priority;

    @Column(name = "condition_type", nullable = false)
    private String conditionType; // ALWAYS, AUTH_METHOD, COST_THRESHOLD

    @Column(name = "condition_value")
    private String conditionValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderSignature provider;

    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
