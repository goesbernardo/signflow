package com.signflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.EnvelopeService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.command.CreateEnvelopeCommand;
import com.signflow.domain.model.Envelope;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
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

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignatureController.class)
@AutoConfigureMockMvc(addFilters = false) // Desabilita filtros de segurança para simplificar o teste unitário do controller
public class SignatureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EnvelopeService envelopeService;

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
    void shouldCreateEnvelopeSuccessfully() throws Exception {
        // Arrange
        CreateEnvelopeCommand command = new CreateEnvelopeCommand();
        command.setName("Test Contract");

        Envelope mockResponse = Envelope.builder()
                .externalId(UUID.randomUUID().toString())
                .name("Test Contract")
                .status(Status.ACTIVE)
                .created(OffsetDateTime.now())
                .build();

        when(envelopeService.createEnvelope(any(CreateEnvelopeCommand.class), eq(ProviderSignature.CLICKSIGN)))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/signatures")
                        .header("provider", "CLICKSIGN")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Contract"))
                .andExpect(jsonPath("$.externalId").exists());
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenProviderHeaderIsMissing() throws Exception {
        CreateEnvelopeCommand command = new CreateEnvelopeCommand();
        command.setName("Test Contract");

        mockMvc.perform(post("/api/v1/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }
}
