package com.signflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.api.controller.AuthController;
import com.signflow.api.dto.LoginRequest;
import com.signflow.api.dto.MfaVerifyRequest;
import com.signflow.application.service.LoginAttemptService;
import com.signflow.application.service.MfaService;
import com.signflow.application.service.PasswordPolicyService;
import com.signflow.application.service.AuditLogService;
import com.signflow.infrastructure.persistence.entity.UserRole;
import java.util.Optional;
import java.util.Set;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.infrastructure.persistence.repository.UserRepository;
import com.signflow.application.service.RefreshTokenService;
import com.signflow.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private LocaleResolver localeResolver;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuditLogService auditLogService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private LoginAttemptService loginAttemptService;

    @MockBean
    private MfaService mfaService;

    @MockBean
    private PasswordPolicyService passwordPolicyService;

    @Test
    @DisplayName("Passo 1: Deve iniciar login e retornar mfaRequired")
    void shouldStartLoginAndReturnMfaRequired() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("user")
                .password("password123")
                .build();

        UserEntity user = UserEntity.builder()
                .username("user")
                .roles(Set.of(UserRole.USER))
                .passwordChangedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordPolicyService.isPasswordExpired(any())).thenReturn(false);
        when(jwtUtils.generateMfaToken("user")).thenReturn("mocked-mfa-token");

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mfaRequired").value(true))
                .andExpect(jsonPath("$.mfaToken").value("mocked-mfa-token"));

        verify(mfaService).sendEmailCode(user);
    }

    @Test
    @DisplayName("Passo 2: Deve verificar código MFA e retornar tokens finais")
    void shouldVerifyMfaAndReturnTokens() throws Exception {
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest("mocked-mfa-token", "123456");

        UserEntity user = UserEntity.builder()
                .username("user")
                .roles(Set.of(UserRole.USER))
                .build();

        com.signflow.infrastructure.persistence.entity.RefreshTokenEntity refreshToken = 
                com.signflow.infrastructure.persistence.entity.RefreshTokenEntity.builder()
                .token("final-refresh-token")
                .build();

        when(jwtUtils.extractUsername("mocked-mfa-token")).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(mfaService.verifyEmailCode(any(), anyString())).thenReturn(true);
        when(jwtUtils.generateToken(any(UserEntity.class))).thenReturn("final-access-token");
        when(refreshTokenService.createRefreshToken("user")).thenReturn(refreshToken);

        mockMvc.perform(post("/v1/auth/mfa/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("final-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("final-refresh-token"))
                .andExpect(jsonPath("$.mfaRequired").value(false));
    }

    @Test
    @DisplayName("Deve falhar no login quando a senha está expirada")
    void shouldFailLoginWhenPasswordExpired() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("admin123")
                .build();

        UserEntity user = UserEntity.builder()
                .username("admin")
                .roles(Set.of(UserRole.ADMIN))
                .passwordChangedAt(LocalDateTime.now().minusDays(91))
                .build();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(passwordPolicyService.isPasswordExpired(any())).thenReturn(true);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("senha expirada. Por favor, altere sua senha."));
    }
}
