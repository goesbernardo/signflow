package com.signflow.api.controller;

import com.signflow.api.dto.*;
import com.signflow.application.service.*;
import com.signflow.config.JwtUtils;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.infrastructure.persistence.entity.RefreshTokenEntity;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import com.signflow.infrastructure.persistence.entity.UserRole;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para gestão de acesso e tokens")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final MfaService mfaService;
    private final HttpServletRequest request;

    private final PasswordPolicyService passwordPolicyService;

    @Operation(summary = "Realizar login", description = "Autentica um usuário e retorna um token JWT ou solicita MFA.")
    @ApiResponse(responseCode = "200", description = "Login realizado com sucesso ou MFA requerido")
    @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    @ApiResponse(responseCode = "423", description = "Conta bloqueada temporariamente")
    @PostMapping("/login")
    @RateLimiter(name = "loginRateLimiter")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {

        loginAttemptService.checkLock(request.username());

        // Autentica credenciais
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));

        UserEntity user = userRepository.findByUsername(request.username()).orElseThrow(() -> new DomainException(DomainErrorCode.USER_NOT_FOUND, "Usuário não encontrado"));

        // Verifica senha expirada
        if (passwordPolicyService.isPasswordExpired(user)) {
            throw new DomainException(DomainErrorCode.PASSWORD_EXPIRED, "senha expirada. Por favor, altere sua senha.");
        }

        // SEMPRE envia código por e-mail — independente da role
         mfaService.sendEmailCode(user);

        String mfaToken = jwtUtils.generateMfaToken(user.getUsername());

        return ResponseEntity.ok(LoginResponse.builder()
                .mfaRequired(true)
                .mfaToken(mfaToken)
                .build());
    }

    @Operation(summary = "Verificar código MFA", description = "Valida o código MFA (TOTP ou E-mail) e retorna os tokens de acesso.")
    @ApiResponse(responseCode = "200", description = "MFA validado com sucesso")
    @ApiResponse(responseCode = "401", description = "Código inválido ou token expirado")
    @PostMapping("/mfa/verify")
    public ResponseEntity<LoginResponse> verifyMfa(@RequestBody @Valid MfaVerifyRequest request) {

        String username = jwtUtils.extractUsername(request.mfaToken());
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new DomainException(DomainErrorCode.USER_NOT_FOUND, "Usuário não encontrado"));

        // SEMPRE valida por e-mail — remove qualquer verificação TOTP
        boolean isValid = mfaService.verifyEmailCode(user, request.code());

        if (!isValid) {
            throw new DomainException(DomainErrorCode.INVALID_CREDENTIALS, "Código inválido ou expirado");
        }

        String accessToken  = jwtUtils.generateToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(username).getToken();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return ResponseEntity.ok(LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .mfaRequired(false)
                .build());
    }

    @Operation(summary = "Atualizar access token", description = "Gera um novo access token a partir de um refresh token válido.")
    @ApiResponse(responseCode = "200", description = "Token atualizado com sucesso")
    @ApiResponse(responseCode = "401", description = "Refresh token inválido ou expirado")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.refreshToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshTokenEntity::getUser)
                .map(user -> {
                    String accessToken = jwtUtils.generateToken(user);
                    return ResponseEntity.ok(LoginResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.refreshToken())
                            .build());
                })
                .orElseThrow(() -> new DomainException(DomainErrorCode.REFRESH_TOKEN_NOT_FOUND, "Refresh token não encontrado"));
    }

    @Operation(summary = "Realizar logout", description = "Invalida o refresh token do usuário.")
    @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest logoutRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        
        refreshTokenService.findByToken(logoutRequest.refreshToken()).ifPresent(token -> {
            String details = "Logout realizado com sucesso";
            UserEntity user = token.getUser();
            if (user != null && user.getLastLoginAt() != null) {
                Duration duration = Duration.between(user.getLastLoginAt(), LocalDateTime.now());
                details += ". Duração da sessão: " + duration.toMinutes() + " minutos";
            }
            auditLogService.log(username, "LOGOUT", "TOKEN", logoutRequest.refreshToken(), details, request.getRemoteAddr(), request.getHeader("User-Agent"));
        });

        refreshTokenService.deleteByToken(logoutRequest.refreshToken());
        log.info("Logout realizado com sucesso para o token: {}", logoutRequest.refreshToken());
        return ResponseEntity.noContent().build();
    }





}
