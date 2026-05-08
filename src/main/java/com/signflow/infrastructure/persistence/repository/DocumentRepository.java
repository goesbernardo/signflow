package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    Optional<DocumentEntity> findByExternalId(String externalId);
    List<DocumentEntity> findAllByEnvelopeExternalId(String externalId);
}
