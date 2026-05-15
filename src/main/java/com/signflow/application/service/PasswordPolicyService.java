package com.signflow.application.service;

import com.signflow.infrastructure.persistence.entity.PasswordHistoryEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.PasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PasswordPolicyService {

    private final PasswordHistoryRepository passwordHistoryRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int HISTORY_LIMIT = 5;
    private static final int EXPIRATION_DAYS = 90;

    public void checkPasswordHistory(UserEntity user, String newPassword) {
        List<PasswordHistoryEntity> history = passwordHistoryRepository.findLastPasswords(user.getId(), HISTORY_LIMIT);
        
        for (PasswordHistoryEntity entry : history) {
            if (passwordEncoder.matches(newPassword, entry.getPasswordHash())) {
                throw new com.signflow.domain.exception.DomainException(
                    com.signflow.domain.exception.DomainErrorCode.PASSWORD_ALREADY_USED,
                    "A nova senha não pode ser igual às últimas " + HISTORY_LIMIT + " senhas utilizadas"
                );
            }
        }
    }

    @Transactional
    public void addToHistory(UserEntity user, String passwordHash) {
        PasswordHistoryEntity historyEntry = PasswordHistoryEntity.builder()
                .user(user)
                .passwordHash(passwordHash)
                .createdAt(LocalDateTime.now())
                .build();
        passwordHistoryRepository.save(historyEntry);
    }

    public boolean isPasswordExpired(UserEntity user) {
        if (user.getPasswordChangedAt() == null) {
            return false;
        }
        return user.getPasswordChangedAt().plusDays(EXPIRATION_DAYS).isBefore(LocalDateTime.now());
    }
}
