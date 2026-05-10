package com.signflow.repository;

import com.signflow.entity.EnvelopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureDocumentRepository extends JpaRepository<EnvelopeEntity, Long> {

    Optional<EnvelopeEntity> findByExternalId(String externalId);
}
