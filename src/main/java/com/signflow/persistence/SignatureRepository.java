package com.signflow.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<EnvelopeEntity, Long> {

    Optional<EnvelopeEntity> findByExternalId(String externalId);
}
