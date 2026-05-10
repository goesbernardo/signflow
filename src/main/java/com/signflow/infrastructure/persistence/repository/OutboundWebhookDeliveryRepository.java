package com.signflow.infrastructure.persistence.repository;

import com.signflow.enums.DeliveryStatus;
import com.signflow.infrastructure.persistence.entity.OutboundWebhookDeliveryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboundWebhookDeliveryRepository extends JpaRepository<OutboundWebhookDeliveryEntity, Long> {
    List<OutboundWebhookDeliveryEntity> findByEnvelopeExternalIdOrderByCreatedAtDesc(String externalId);

    List<OutboundWebhookDeliveryEntity> findByStatusAndNextAttemptAtBeforeAndAttemptsLessThan(
            DeliveryStatus status, LocalDateTime now, int maxAttempts);
}
