package com.signflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.EnvelopeService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.command.AddDocumentCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateFullEnvelopeCommand;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Requirement;
import com.signflow.domain.model.Signer;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void shouldCreateFullEnvelopeSuccessfully() throws Exception {
        CreateFullEnvelopeCommand command = CreateFullEnvelopeCommand.builder()
                .name("Full Contract")
                .documents(List.of(AddDocumentCommand.builder().filename("doc.pdf").contentBase64("abc").build()))
                .signers(List.of(AddSignerCommand.builder().name("John").email("john@test.com").build()))
                .autoActivate(true)
                .build();

        Envelope mockResponse = Envelope.builder()
                .externalId(UUID.randomUUID().toString())
                .name("Full Contract")
                .status(Status.ACTIVE)
                .build();

        when(envelopeService.createFullEnvelope(any(CreateFullEnvelopeCommand.class), eq(ProviderSignature.CLICKSIGN)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Full Contract"))
                .andExpect(jsonPath("$.externalId").exists());
    }
}
