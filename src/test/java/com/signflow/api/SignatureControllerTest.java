package com.signflow.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.application.EnvelopeService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.command.ActivateEnvelopeCommand;
import com.signflow.domain.command.AddRequirementCommand;
import com.signflow.domain.command.AddSignerCommand;
import com.signflow.domain.command.CreateEnvelopeCommand;
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
    void shouldCreateEnvelopeSuccessfully() throws Exception {
        // Arrange
        CreateEnvelopeCommand command = CreateEnvelopeCommand.builder()
                .name("Test Contract")
                .build();

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
        CreateEnvelopeCommand command = CreateEnvelopeCommand.builder()
                .name("Test Contract")
                .build();

        mockMvc.perform(post("/api/v1/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldAddRequirementWithQualificationSuccessfully() throws Exception {
        String envelopeId = UUID.randomUUID().toString();
        AddRequirementCommand command = AddRequirementCommand.builder()
                .documentId("doc-123")
                .signerId("signer-456")
                .action("sign")
                .auth("email")
                .role("witness")
                .build();

        Requirement mockResponse = Requirement.builder()
                .externalId(UUID.randomUUID().toString())
                .build();

        when(envelopeService.addRequirement(eq(envelopeId), any(AddRequirementCommand.class), eq(ProviderSignature.CLICKSIGN)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/signatures/{externalId}/requirements", envelopeId)
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.externalId").exists());
    }

    @Test
    @WithMockUser
    void shouldActivateEnvelopeSuccessfully() throws Exception {
        String envelopeId = UUID.randomUUID().toString();
        ActivateEnvelopeCommand command = ActivateEnvelopeCommand.builder()
                .signerId("signer-123")
                .documentId("doc-456")
                .role("sign")
                .auth("email")
                .build();

        mockMvc.perform(put("/api/v1/signatures/{externalId}/activate", envelopeId)
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void shouldAddMultipleSignersSuccessfully() throws Exception {
        String envelopeId = UUID.randomUUID().toString();
        List<AddSignerCommand> commands = List.of(
                AddSignerCommand.builder().name("Signer 1").email("signer1@test.com").build(),
                AddSignerCommand.builder().name("Signer 2").email("signer2@test.com").build()
        );

        List<Signer> mockResponse = List.of(
                Signer.builder().externalId(UUID.randomUUID().toString()).name("Signer 1").build(),
                Signer.builder().externalId(UUID.randomUUID().toString()).name("Signer 2").build()
        );

        when(envelopeService.addSigners(eq(envelopeId), any(List.class), eq(ProviderSignature.CLICKSIGN)))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/signatures/{externalId}/signers", envelopeId)
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commands)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Signer 1"))
                .andExpect(jsonPath("$[1].name").value("Signer 2"));
    }
}
