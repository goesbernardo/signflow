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

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler - Mapeamento de DomainErrorCode")
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SignatureService signatureService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private MessageSource messageSource;

    @MockBean
    private LocaleResolver localeResolver;

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
                .andExpect(status().is(expectedStatus));
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
