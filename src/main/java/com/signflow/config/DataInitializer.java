package com.signflow.config;

import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.entity.UserRole;
import com.signflow.application.service.PasswordPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.signflow.infrastructure.persistence.repository.UserRepository;

import java.util.Set;

@Configuration
@Profile({"local", "dev", "prod"})
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;

    @org.springframework.beans.factory.annotation.Value("${security.admin.username}")
    private String adminUsername;

    @org.springframework.beans.factory.annotation.Value("${security.admin.password}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                UserEntity admin = UserEntity.builder()
                        .username(adminUsername)
                        .name("Administrator")
                        .password(passwordEncoder.encode(adminPassword))
                        .email("admin@signflow.api.br")
                        .tenantId("SYSTEM")
                        .consentAt(java.time.LocalDateTime.now())
                        .roles(Set.of(UserRole.ADMIN, UserRole.OPERATOR))
                        .passwordChangedAt(java.time.LocalDateTime.now())
                        .build();
                userRepository.save(admin);
                passwordPolicyService.addToHistory(admin, admin.getPassword());
            }
        };
    }
}
