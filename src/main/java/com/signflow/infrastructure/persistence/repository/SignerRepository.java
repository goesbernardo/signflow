package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.SignerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SignerRepository extends JpaRepository<SignerEntity, Long> {
    Optional<SignerEntity> findByExternalId(String externalId);
    List<SignerEntity> findAllByEnvelopeExternalId(String externalId);
}
