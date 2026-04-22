package com.signflow.repository;

import com.signflow.entity.EnvelopeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClickSignDocumentRepository extends JpaRepository<EnvelopeEntity,String> {
}
