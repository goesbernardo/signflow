package com.signflow.application.service.impl;

import com.signflow.application.service.EmailService;
import com.signflow.application.service.MfaService;
import com.signflow.infrastructure.persistence.entity.MfaCodeEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.MfaCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaServiceImpl implements MfaService {

    private final MfaCodeRepository mfaCodeRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void sendEmailCode(UserEntity user) {
        String code = String.format("%06d", secureRandom.nextInt(1000000));
        
        // Remove códigos antigos pendentes
        mfaCodeRepository.deleteByUser(user);

        MfaCodeEntity mfaCode = MfaCodeEntity.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();

        mfaCodeRepository.save(mfaCode);
        emailService.sendMfaCode(user, code);
    }

    @Override
    @Transactional
    public boolean verifyEmailCode(UserEntity user, String code) {
        return mfaCodeRepository.findFirstByUserAndCodeAndUsedFalseOrderByExpiresAtDesc(user, code)
                .map(mfaCode -> {
                    if (mfaCode.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return false;
                    }
                    mfaCode.setUsed(true);
                    mfaCodeRepository.save(mfaCode);
                    return true;
                })
                .orElse(false);
    }
}
