package com.signflow.persistence;

import com.signflow.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignatureRepository extends JpaRepository<EnvelopeEntity, Long> {

    Optional<EnvelopeEntity> findByExternalId(String externalId);

    Page<EnvelopeEntity> findAllByUserId(String userId, Pageable pageable);

    Page<EnvelopeEntity> findAllByUserIdAndStatus(String userId, Status status, Pageable pageable);
}
