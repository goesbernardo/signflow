package com.signflow.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@Getter
@Setter
@Validated
public class JwtConfig {
    @NotBlank
    private String secret;
    private long expiration;
}
