package com.signflow.api;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.exception.DomainErrorCode;
import com.signflow.domain.exception.DomainException;
import com.signflow.enums.ProviderSignature;
import com.signflow.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class SignatureRemindIntegrationTest {

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

    @Test
    @WithMockUser
    void shouldReturnNoContentWhenRemindIsSuccessful() throws Exception {
        String envelopeId = "env_123";
        String signerId = "signer_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        mockMvc.perform(post("/api/v1/signatures/{envelopeId}/signers/{signerId}/remind", envelopeId, signerId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(signatureService).remindSigner(envelopeId, signerId, provider);
    }

    @Test
    @WithMockUser
    void shouldReturnTooManyRequestsWhenRemindingWithinOneHour() throws Exception {
        String envelopeId = "env_123";
        String signerId = "signer_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        // Atualmente o GlobalExceptionHandler mapeia DomainException para 400 ou 409, 
        // mas o requisito do service não especificou 429 explicitamente no handler ainda.
        // Vamos ver como o GlobalExceptionHandler trata.
        
        doThrow(new DomainException(DomainErrorCode.REMINDER_RATE_LIMIT, "Um lembrete já foi enviado na última hora para este signatário"))
                .when(signatureService).remindSigner(envelopeId, signerId, provider);

        mockMvc.perform(post("/api/v1/signatures/{envelopeId}/signers/{signerId}/remind", envelopeId, signerId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests());
    }
}
