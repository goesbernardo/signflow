package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.EnvelopeEntity;
import com.signflow.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvelopeRepository extends JpaRepository<EnvelopeEntity, Long> {

    Optional<EnvelopeEntity> findByExternalIdAndTenantId(String externalId, String tenantId);
    Optional<EnvelopeEntity> findByExternalId(String externalId);

    Page<EnvelopeEntity> findAllByUserIdAndTenantId(String userId, String tenantId, Pageable pageable);

    Page<EnvelopeEntity> findAllByUserIdAndStatusAndTenantId(String userId, Status status, String tenantId, Pageable pageable);
}
