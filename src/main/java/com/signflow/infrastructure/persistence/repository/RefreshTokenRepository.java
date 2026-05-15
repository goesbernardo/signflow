package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.RefreshTokenEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
    int deleteByToken(String token);
}
