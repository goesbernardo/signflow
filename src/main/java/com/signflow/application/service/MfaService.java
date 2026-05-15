package com.signflow.application.service;

import com.signflow.infrastructure.persistence.entity.UserEntity;

public interface MfaService {
    String generateSecret();
    String generateQrCodeUri(UserEntity user);
    boolean verifyTotp(UserEntity user, String code);
    
    void sendEmailCode(UserEntity user);
    boolean verifyEmailCode(UserEntity user, String code);
}
