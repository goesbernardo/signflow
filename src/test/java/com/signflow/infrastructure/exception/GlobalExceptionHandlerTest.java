package com.signflow.infrastructure.exception;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.enums.ProviderSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import com.signflow.api.controller.AuthController;
import com.signflow.api.controller.UserController;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, AuthController.class, UserController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler - Mapeamento de Exceções")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignatureService signatureService;

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
    private com.signflow.application.service.AuditLogService auditLogService;

    @ParameterizedTest(name = "Erro {0} deve retornar HTTP {1}")
    @MethodSource("provideErrorCodesAndStatus")
    @WithMockUser
    void shouldMapDomainErrorCodeToHttpStatus(DomainErrorCode errorCode, int expectedStatus) throws Exception {
        String externalId = "env-123";
        
        doThrow(new DomainException(errorCode, "Mensagem de erro"))
                .when(signatureService).activateEnvelope(eq(externalId), eq(ProviderSignature.CLICKSIGN));

        mockMvc.perform(post("/api/v1/signatures/{externalId}/activate", externalId)
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.details[0].message").value("Mensagem de erro"))
                .andExpect(jsonPath("$.details[0].code").value(errorCode.name()));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Deve retornar 401 para BadCredentialsException")
    void shouldReturn401ForBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        when(messageSource.getMessage(eq("error.bad_credentials"), any(), any()))
                .thenReturn("Credenciais inválidas");
        when(messageSource.getMessage(eq("error.bad_credentials_message"), any(), any()))
                .thenReturn("Usuário ou senha incorretos");

        String loginJson = "{\"username\":\"admin\", \"password\":\"wrong\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Credenciais inválidas"))
                .andExpect(jsonPath("$.message").value("Usuário ou senha incorretos"))
                .andExpect(jsonPath("$.details[0].field").value("credentials"))
                .andExpect(jsonPath("$.details[0].message").value("Invalid credentials"))
                .andExpect(jsonPath("$.details[0].code").value("BAD_CREDENTIALS"));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Deve retornar 403 para AccessDeniedException")
    void shouldReturn403ForAccessDenied() throws Exception {
        doThrow(new AccessDeniedException("Forbidden"))
                .when(signatureService).deleteMe();

        when(messageSource.getMessage(eq("error.access_denied"), any(), any()))
                .thenReturn("Acesso negado");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/v1/users/me"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Acesso negado"))
                .andExpect(jsonPath("$.details[0].field").value("access"))
                .andExpect(jsonPath("$.details[0].message").value("Forbidden"))
                .andExpect(jsonPath("$.details[0].code").value("ACCESS_DENIED"));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Deve retornar 500 com detalhes para exceção genérica")
    void shouldReturn500ForGenericException() throws Exception {
        when(signatureService.getDocuments(anyString(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        when(messageSource.getMessage(eq("error.internal_server_error"), any(), any()))
                .thenReturn("Erro interno");

        mockMvc.perform(get("/api/v1/signatures/123/documents")
                        .header("provider", "CLICKSIGN"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.details[0].message").value("Unexpected error"));
    }

    private static Stream<Arguments> provideErrorCodesAndStatus() {
        return Stream.of(
                Arguments.of(DomainErrorCode.NOT_FOUND, 404),
                Arguments.of(DomainErrorCode.INVALID_ENVELOPE_STATUS, 409),
                Arguments.of(DomainErrorCode.REMINDER_RATE_LIMIT, 429),
                Arguments.of(DomainErrorCode.INVALID_AUTH_METHOD, 400),
                Arguments.of(DomainErrorCode.BUSINESS_RULE_VIOLATION, 400)
        );
    }
}
