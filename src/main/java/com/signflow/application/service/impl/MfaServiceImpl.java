package com.signflow.application.service.impl;

import com.signflow.application.service.EmailService;
import com.signflow.application.service.MfaService;
import com.signflow.infrastructure.persistence.entity.MfaCodeEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.MfaCodeRepository;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaServiceImpl implements MfaService {

    private final SecretGenerator secretGenerator;
    private final QrGenerator qrGenerator;
    private final CodeVerifier codeVerifier;
    private final MfaCodeRepository mfaCodeRepository;
    private final EmailService emailService;
    private final Random random = new Random();

    @Override
    public String generateSecret() {
        return secretGenerator.generate();
    }

    @Override
    public String generateQrCodeUri(UserEntity user) {
        QrData data = new QrData.Builder()
                .label(user.getUsername())
                .secret(user.getMfaSecret())
                .issuer("SignFlow")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            return qrGenerator.generate(data);
        } catch (QrGenerationException e) {
            log.error("Erro ao gerar QR Code para o usuário: {}", user.getUsername(), e);
            return null;
        }
    }

    @Override
    public boolean verifyTotp(UserEntity user, String code) {
        if (user.getMfaSecret() == null) return false;
        return codeVerifier.isValidCode(user.getMfaSecret(), code);
    }

    @Override
    @Transactional
    public void sendEmailCode(UserEntity user) {
        String code = String.format("%06d", random.nextInt(1000000));
        
        // Remove códigos antigos pendentes
        mfaCodeRepository.deleteByUser(user);

        MfaCodeEntity mfaCode = MfaCodeEntity.builder()
                .user(user)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
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
