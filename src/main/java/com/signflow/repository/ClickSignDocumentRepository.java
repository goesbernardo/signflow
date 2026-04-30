package com.signflow.repository;

import com.signflow.entity.EnvelopeEntity;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClickSignDocumentRepository extends JpaRepository<EnvelopeEntity,String> {

    Optional<EnvelopeEntity> findByExternalId(String externalId);
}
