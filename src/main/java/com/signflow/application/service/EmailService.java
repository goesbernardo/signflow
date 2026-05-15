package com.signflow.application.service;

import com.signflow.infrastructure.persistence.entity.UserEntity;

public interface EmailService {
    void sendMfaCode(UserEntity user, String code);
}
