package com.signflow.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignerRepository extends JpaRepository<SignerEntity, Long> {
    Optional<SignerEntity> findByExternalId(String externalId);
}
