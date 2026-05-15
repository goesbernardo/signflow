package com.signflow.api.controller;

import com.signflow.api.dto.ChangePasswordRequest;
import com.signflow.application.port.in.SignatureService;
import com.signflow.application.service.AuditLogService;
import com.signflow.application.service.PasswordPolicyService;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users")
@Tag(name = "User Management", description = "Endpoints for managing users and privacy")
public class UserController {

    private final SignatureService signatureService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final PasswordPolicyService passwordPolicyService;
    private final HttpServletRequest request;

    @DeleteMapping("/me")
    @Operation(summary = "Excluir minha conta (LGPD)", 
               description = "Realiza o soft delete dos dados do usuário em conformidade com o direito ao esquecimento da LGPD.")
    public ResponseEntity<Void> deleteMe() {
        log.info("Recebendo solicitação de exclusão de conta.");
        signatureService.deleteMe();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário autenticado após validar a senha atual.")
    @ApiResponse(responseCode = "200", description = "Senha alterada com sucesso")
    @ApiResponse(responseCode = "400", description = "Senha atual incorreta ou nova senha inválida")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changeRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException(DomainErrorCode.USER_NOT_FOUND, "Usuário não encontrado"));

        if (!passwordEncoder.matches(changeRequest.currentPassword(), user.getPassword())) {
            auditLogService.log(username, "CHANGE_PASSWORD_FAILURE", "USER", username, "Tentativa de alteração de senha: senha atual incorreta", request.getRemoteAddr(), request.getHeader("User-Agent"));
            throw new DomainException(DomainErrorCode.INVALID_CREDENTIALS, "Senha atual incorreta");
        }

        passwordPolicyService.checkPasswordHistory(user, changeRequest.newPassword());

        user.setPassword(passwordEncoder.encode(changeRequest.newPassword()));
        user.setPasswordChangedAt(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        passwordPolicyService.addToHistory(user, user.getPassword());

        log.info("Senha alterada com sucesso para o usuário: {}", username);
        auditLogService.log(username, "CHANGE_PASSWORD_SUCCESS", "USER", username, "Senha alterada com sucesso", request.getRemoteAddr(), request.getHeader("User-Agent"));

        return ResponseEntity.ok().build();
    }
}
