package com.signflow.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvelopeEventRepository extends JpaRepository<EnvelopeEventEntity, Long> {
}
