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
public class SignatureActivationIntegrationTest {

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
    void shouldReturnNoContentWhenActivationIsSuccessful() throws Exception {
        String externalId = "env_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        mockMvc.perform(post("/api/v1/signatures/{externalId}/activate", externalId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(signatureService).activateEnvelope(externalId, provider);
    }

    @Test
    @WithMockUser
    void shouldReturnConflictWhenEnvelopeIsNotInDraftStatus() throws Exception {
        String externalId = "env_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        doThrow(new DomainException(DomainErrorCode.INVALID_ENVELOPE_STATUS, "Somente envelopes em rascunho (DRAFT) podem ser ativados."))
                .when(signatureService).activateEnvelope(externalId, provider);

        mockMvc.perform(post("/api/v1/signatures/{externalId}/activate", externalId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }
}
