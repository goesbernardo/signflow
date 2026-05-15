package com.signflow.application.service;

import com.signflow.config.JwtConfig;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.infrastructure.persistence.entity.RefreshTokenEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.RefreshTokenRepository;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtConfig jwtConfig;

    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshTokenEntity createRefreshToken(String username) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException(DomainErrorCode.USER_NOT_FOUND, "Usuário não encontrado: " + username));

        refreshTokenRepository.deleteByUser(user);

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpiration()))
                .token(UUID.randomUUID().toString())
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new DomainException(DomainErrorCode.REFRESH_TOKEN_EXPIRED, "Refresh token expirado. Por favor, realize o login novamente.");
        }
        return token;
    }

    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
