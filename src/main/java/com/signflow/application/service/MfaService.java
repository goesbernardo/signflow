package com.signflow.application.service;

import com.signflow.infrastructure.persistence.entity.UserEntity;

public interface MfaService {
    void sendEmailCode(UserEntity user);
    boolean verifyEmailCode(UserEntity user, String code);
}
