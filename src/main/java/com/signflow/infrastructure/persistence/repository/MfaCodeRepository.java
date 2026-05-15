package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.MfaCodeEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MfaCodeRepository extends JpaRepository<MfaCodeEntity, Long> {
    Optional<MfaCodeEntity> findFirstByUserAndCodeAndUsedFalseOrderByExpiresAtDesc(UserEntity user, String code);
    void deleteByUser(UserEntity user);
}
