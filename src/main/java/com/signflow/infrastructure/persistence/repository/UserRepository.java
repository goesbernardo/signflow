package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    @Query("SELECT u FROM UserEntity u WHERE u.username = :username AND u.deleted_at IS NULL")
    Optional<UserEntity> findByUsername(@Param("username") String username);
}
