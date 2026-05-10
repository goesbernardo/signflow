package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
    List<AuditLogEntity> findByUserId(String userId);
    List<AuditLogEntity> findByResourceTypeAndResourceId(String resourceType, String resourceId);
}
