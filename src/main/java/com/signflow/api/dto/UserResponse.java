package com.signflow.api.dto;

import com.signflow.infrastructure.persistence.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Set;

@Builder
public record UserResponse(
        Long id,
        String username,
        String name,
        String email,
        Set<UserRole> roles
) {}
