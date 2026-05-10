package com.signflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.command.AddNotifierCommand;
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

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class NotifierIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void shouldReturnCreatedWhenAddingNotifierIsSuccessful() throws Exception {
        String envelopeId = "env_123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;
        AddNotifierCommand command = AddNotifierCommand.builder()
                .email("observer@example.com")
                .name("Observer Test")
                .build();

        mockMvc.perform(post("/api/v1/signatures/{envelopeId}/notifiers", envelopeId)
                        .header("provider", provider.name())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());

        verify(signatureService).addNotifier(envelopeId, command, provider);
    }
}
