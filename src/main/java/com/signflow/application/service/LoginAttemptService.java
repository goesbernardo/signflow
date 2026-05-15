package com.signflow.application.service;

import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.infrastructure.persistence.entity.LoginAttemptEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.LoginAttemptRepository;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final LoginAttemptRepository loginAttemptRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void recordAttempt(String username, String ip, boolean success, String userAgent) {
        LoginAttemptEntity attempt = LoginAttemptEntity.builder()
                .username(username)
                .ipAddress(ip)
                .success(success)
                .build();
        loginAttemptRepository.save(attempt);

        if (!success) {
            handleFailedAttempt(username, ip, userAgent);
        } else {
            resetAttempts(username);
        }
    }

    private void handleFailedAttempt(String username, String ip, String userAgent) {
        // Regra 1: 5 falhas em 5 min -> 15 min bloqueio
        if (countFailures(username, ip, 5) >= 5) {
            lockUser(username, 15, "Bloqueio de 15 minutos (5 falhas em 5min)");
        }
        // Regra 2: 10 falhas em 1 hora -> 1 hora bloqueio
        else if (countFailures(username, ip, 60) >= 10) {
            lockUser(username, 60, "Bloqueio de 1 hora (10 falhas em 1h)");
        }
        // Regra 3: 20 falhas em 1 dia -> 24 horas bloqueio + alerta
        else if (countFailures(username, ip, 1440) >= 20) {
            log.error("ALERTA DE SEGURANÇA: 20 tentativas falhas para o usuário {} ou IP {} em 24h", username, ip);
            lockUser(username, 1440, "Bloqueio de 24 horas (20 falhas em 24h)");
        }

        auditLogService.log(username, "LOGIN_FAILURE", "USER", username, "Tentativa de login falha", ip, userAgent);
    }

    private long countFailures(String username, String ip, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        long byUser = loginAttemptRepository.countByUsernameAndSuccessAndCreatedAtAfter(username, false, since);
        long byIp = loginAttemptRepository.countByIpAddressAndSuccessAndCreatedAtAfter(ip, false, since);
        return Math.max(byUser, byIp);
    }

    private void lockUser(String username, int minutes, String reason) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(minutes));
            userRepository.save(user);
            log.warn("Usuário {} bloqueado por {} minutos: {}", username, minutes, reason);
        });
    }

    private void resetAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (user.getLockedUntil() != null) {
                user.setLockedUntil(null);
                userRepository.save(user);
            }
        });
    }

    public void checkLock(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            if (!user.isAccountNonLocked()) {
                throw new DomainException(DomainErrorCode.ACCOUNT_LOCKED, 
                    "Sua conta está temporariamente bloqueada até " + user.getLockedUntil());
            }
        });
    }
}
