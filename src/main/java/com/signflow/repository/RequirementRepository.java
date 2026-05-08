package com.signflow.repository;

import com.signflow.domain.entity.RequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequirementRepository extends JpaRepository<RequirementEntity, Long> {
    Optional<RequirementEntity> findByExternalId(String externalId);
    List<RequirementEntity> findAllByEnvelopeExternalId(String externalId);

}
