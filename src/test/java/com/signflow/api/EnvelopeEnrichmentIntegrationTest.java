package com.signflow.api;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.domain.model.Envelope;
import com.signflow.domain.model.Signer;
import com.signflow.enums.ProviderSignature;
import com.signflow.enums.Status;
import com.signflow.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class EnvelopeEnrichmentIntegrationTest {

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
    void shouldReturnEnvelopeWithSignersWhenIncludeSignersIsTrue() throws Exception {
        String externalId = "env-123";
        ProviderSignature provider = ProviderSignature.CLICKSIGN;
        
        Signer signer = Signer.builder()
                .externalId("sig-123")
                .name("Joao")
                .email("joao@ex.com")
                .status("PENDING")
                .build();

        Envelope envelope = Envelope.builder()
                .externalId(externalId)
                .name("Envelope Teste")
                .status(Status.ACTIVE)
                .signers(List.of(signer))
                .build();

        when(signatureService.getEnvelope(eq(externalId), eq(provider), eq(true)))
                .thenReturn(envelope);

        mockMvc.perform(get("/api/v1/signatures/{externalId}", externalId)
                        .header("provider", provider.name())
                        .param("includeSigners", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalId", is(externalId)))
                .andExpect(jsonPath("$.signers", hasSize(1)))
                .andExpect(jsonPath("$.signers[0].name", is("Joao")))
                .andExpect(jsonPath("$.signers[0].status", is("PENDING")));
    }

    @Test
    @WithMockUser
    void shouldReturnListWithSignersWhenIncludeSignersIsTrue() throws Exception {
        Signer signer = Signer.builder()
                .externalId("sig-123")
                .name("Joao")
                .status("PENDING")
                .build();

        Envelope envelope = Envelope.builder()
                .externalId("env-123")
                .name("Envelope Teste")
                .status(Status.ACTIVE)
                .signers(List.of(signer))
                .build();

        Page<Envelope> page = new PageImpl<>(List.of(envelope));

        when(signatureService.listEnvelopes(any(), any(Pageable.class), eq(true)))
                .thenReturn(page);

        mockMvc.perform(get("/api/v1/signatures")
                        .param("includeSigners", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].signers", hasSize(1)))
                .andExpect(jsonPath("$.content[0].signers[0].name", is("Joao")));
    }
}
