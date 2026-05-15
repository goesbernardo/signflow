package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.PasswordHistoryEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryEntity, Long> {

    @Query(value = "SELECT * FROM password_histories WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<PasswordHistoryEntity> findLastPasswords(Long userId, int limit);

    void deleteByUserId(Long userId);
}
