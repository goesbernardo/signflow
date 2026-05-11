package com.signflow.api;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class SmartRoutingIntegrationTest {

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
    void shouldAllowRequestWithoutProviderHeader() throws Exception {
        String externalId = "env_123";

        mockMvc.perform(get("/v1/signatures/{externalId}", externalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(signatureService).getEnvelope(eq(externalId), isNull(), eq(false));
    }

    @Test
    @WithMockUser
    void shouldAllowCreateFullEnvelopeWithoutProviderHeader() throws Exception {
        String json = """
                {
                  "name": "Contrato Teste",
                  "autoActivate": true,
                  "documents": [],
                  "signers": [],
                  "requirements": []
                }
                """;

        mockMvc.perform(post("/v1/signatures/create-activate-envelope")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(signatureService).createFullEnvelope(any(), isNull());
    }

    @Test
    @WithMockUser
    void shouldRespectProviderHeaderWhenPresent() throws Exception {
        String externalId = "env_123";
        ProviderSignature provider = ProviderSignature.DOCUSIGN;

        mockMvc.perform(get("/v1/signatures/{externalId}", externalId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(signatureService).getEnvelope(eq(externalId), eq(provider), eq(false));
    }
}
