package com.signflow.infrastructure.persistence.repository;

import com.signflow.infrastructure.persistence.entity.LoginAttemptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttemptEntity, Long> {
    
    long countByUsernameAndSuccessAndCreatedAtAfter(String username, boolean success, LocalDateTime since);
    
    long countByIpAddressAndSuccessAndCreatedAtAfter(String ipAddress, boolean success, LocalDateTime since);
}
