package com.signflow.api.controller;

import com.signflow.api.dto.AdminRegisterRequest;
import com.signflow.api.dto.AdminRegisterResponse;
import com.signflow.api.dto.UserResponse;
import com.signflow.application.service.AuditLogService;
import com.signflow.application.service.PasswordPolicyService;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.entity.UserRole;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Endpoints de administração do sistema")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordPolicyService passwordPolicyService;
    private final AuditLogService auditLogService;

    @Operation(summary = "Registrar novo usuário",
               description = "Cria um novo usuário na plataforma. Acesso restrito a ADMIN.")
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou role inválida")
    @ApiResponse(responseCode = "403", description = "Sem permissão (role ADMIN obrigatório)")
    @ApiResponse(responseCode = "409", description = "Username ou e-mail já existente")
    @PostMapping("/users")
    public ResponseEntity<AdminRegisterResponse> registerUser(@RequestBody @Valid AdminRegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new DomainException(DomainErrorCode.USER_ALREADY_EXISTS, "Username já está em uso");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DomainException(DomainErrorCode.EMAIL_ALREADY_EXISTS, "E-mail já está em uso");
        }

        // Role informada no request — ADMIN não pode ser criado via API
        UserRole role = resolveRole(request.role());

        UserEntity newUser = UserEntity.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .tenantId(request.tenantId())
                .roles(Set.of(role))
                .passwordChangedAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        passwordPolicyService.addToHistory(newUser, newUser.getPassword());

        // Auditoria — registra quem criou o usuário
        String adminUsername = SecurityContextHolder.getContext()
                                   .getAuthentication().getName();

        log.info("Usuário {} criado pelo admin {}", request.username(), adminUsername);
        auditLogService.log(adminUsername, "ADMIN_REGISTER", "USER",
                request.username(), "Usuário criado por admin", null, null);

        AdminRegisterResponse response = AdminRegisterResponse.builder()
                .name(newUser.getName())
                .role(role.name())
                .message("usuário criado com sucesso na plataforma signflow")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Impede criação de ADMIN via API — deve ser feito direto no banco
    private UserRole resolveRole(String roleStr) {
        UserRole role = UserRole.valueOf(roleStr.toUpperCase());
        if (role == UserRole.ADMIN) {
            throw new DomainException(DomainErrorCode.BUSINESS_RULE_VIOLATION,
                "Role ADMIN não pode ser atribuída via API. " +
                "Contate o suporte para provisionamento de administradores.");
        }
        return role;
    }

    @GetMapping("/users")
    @Operation(summary = "Listar todos os usuários", description = "Retorna a lista de todos os usuários cadastrados no sistema. Acesso restrito a ADMIN.")
    public ResponseEntity<List<UserResponse>> listAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .name(user.getName())
                        .email(user.getEmail())
                        .roles(user.getRoles())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}
