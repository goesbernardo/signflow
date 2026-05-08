package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.EnvelopeEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvelopeEventRepository extends JpaRepository<EnvelopeEventEntity, Long> {
    List<EnvelopeEventEntity> findAllByEnvelopeExternalIdOrderByOccurredAtAsc(String externalId);
}
