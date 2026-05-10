package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.NotifierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotifierRepository extends JpaRepository<NotifierEntity, Long> {
}
