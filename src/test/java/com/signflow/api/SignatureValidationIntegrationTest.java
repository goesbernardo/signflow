package com.signflow.api;

import com.signflow.service.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
import com.signflow.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class SignatureValidationIntegrationTest {

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
    void shouldReturnBadRequestWhenPhoneNumberIsInvalid() throws Exception {
        String invalidPhoneNumberJson = """
                {
                    "name": "Test",
                    "documents": [{"filename": "doc.pdf", "content_base64": "abc"}],
                    "signers": [
                        {
                            "name": "John",
                            "email": "john@test.com",
                            "phone_number": "12345"
                        }
                    ],
                    "requirements": [
                        {
                            "role": "SIGN",
                            "auth": "sms"
                        }
                    ]
                }
                """;

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Requisição inválida");

        mockMvc.perform(post("/api/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPhoneNumberJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Requisição inválida"))
                .andExpect(jsonPath("$.details[0].field").value("signers[0].phoneNumber"))
                .andExpect(jsonPath("$.details[0].message").value("O número de telefone deve possuir 10 ou 11 números"));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestWhenPhoneNumberHasLetters() throws Exception {
        String invalidPhoneNumberJson = """
                {
                    "name": "Test",
                    "documents": [{"filename": "doc.pdf", "content_base64": "abc"}],
                    "signers": [
                        {
                            "name": "John",
                            "email": "john@test.com",
                            "phone_number": "1199999999a"
                        }
                    ]
                }
                """;

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Requisição inválida");

        mockMvc.perform(post("/api/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPhoneNumberJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("signers[0].phoneNumber"))
                .andExpect(jsonPath("$.details[0].message").value("O número de telefone deve possuir 10 ou 11 números"));
    }

    @Test
    @WithMockUser
    void shouldAcceptValidPhoneNumber() throws Exception {
        String validPhoneNumberJson = """
                {
                    "name": "Test",
                    "documents": [{"filename": "doc.pdf", "content_base64": "abc"}],
                    "signers": [
                        {
                            "name": "John",
                            "email": "john@test.com",
                            "phone_number": "11999999999"
                        }
                    ]
                }
                """;

        when(signatureService.createFullEnvelope(any(), any())).thenReturn(com.signflow.domain.model.Envelope.builder().build());

        mockMvc.perform(post("/api/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPhoneNumberJson))
                .andExpect(status().isCreated());
    }
}
