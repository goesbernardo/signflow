package com.signflow.api;

import com.signflow.api.controller.SignatureController;
import com.signflow.application.port.in.SignatureService;
import com.signflow.config.JwtAuthenticationFilter;
import com.signflow.config.JwtUtils;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({SignatureController.class, GlobalExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
public class SignatureParseErrorIntegrationTest {

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
    void shouldReturnDetailedErrorWhenJsonParseFails() throws Exception {
        String invalidJson = """
                {
                    "name": "Test",
                    "requirements": [
                        {
                            "role": "SIGN",
                            "auth": "invalid_auth"
                        }
                    ]
                }
                """;

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Requisição inválida");

        mockMvc.perform(post("/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Requisição inválida"))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("RequirementAuth inválido")));
    }

    @Test
    @WithMockUser
    void shouldAcceptEmptyAuthAsNull() throws Exception {
        // Se auth for "" e o JsonCreator retornar null, o Jackson aceitará se o campo permitir null
        String jsonWithEmptyAuth = """
                {
                    "name": "Test",
                    "documents": [{"filename": "doc.pdf", "content_base64": "abc"}],
                    "signers": [{"name": "John", "email": "john@test.com", "phone_number": "11999999999"}],
                    "requirements": [
                        {
                            "role": "SIGN",
                            "auth": ""
                        }
                    ]
                }
                """;

        // Precisamos mockar o service para não dar erro de lógica interna se o parse passar
        when(signatureService.createFullEnvelope(any(), any())).thenReturn(com.signflow.domain.model.Envelope.builder().build());

        mockMvc.perform(post("/v1/signatures/create-activate-envelope")
                        .header("provider", "CLICKSIGN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithEmptyAuth))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated());
    }
}
