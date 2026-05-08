package com.signflow.api;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.enums.ProviderSignature;
import com.signflow.infrastructure.exception.ErroDetail;
import com.signflow.infrastructure.exception.GlobalExceptionHandler;
import com.signflow.infrastructure.exception.IntegrationException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class IntegrationExceptionHandlerTest {

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
    void shouldReturnBadGatewayWhenIntegrationExceptionOccurs() throws Exception {
        String externalId = "env_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        List<ErroDetail> details = List.of(
                ErroDetail.builder().code("invalid_token").message("Token inválido").field("header").build()
        );
        IntegrationException integrationEx = new IntegrationException("Erro na integração", "raw response body", details);

        doThrow(integrationEx).when(signatureService).activateEnvelope(eq(externalId), eq(provider));

        mockMvc.perform(post("/api/v1/signatures/{externalId}/activate", externalId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.message").value("Erro na integração"))
                .andExpect(jsonPath("$.details[0].code").value("invalid_token"))
                .andExpect(jsonPath("$.details[0].message").value("Token inválido"))
                .andExpect(jsonPath("$.details[0].field").value("header"));
    }

    @Test
    @WithMockUser
    void shouldReturnInternalServerErrorWhenGenericExceptionOccurs() throws Exception {
        String externalId = "env_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;

        doThrow(new RuntimeException("Crash!")).when(signatureService).activateEnvelope(eq(externalId), eq(provider));

        mockMvc.perform(post("/api/v1/signatures/{externalId}/activate", externalId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocorreu um erro interno inesperado"));
    }
}
