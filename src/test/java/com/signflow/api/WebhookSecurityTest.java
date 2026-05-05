package com.signflow.api;

import com.signflow.adapter.clicksign.webhook.ClickSignHmacValidator;
import com.signflow.adapter.clicksign.webhook.ClickSignWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Filtros de segurança ATIVOS por padrão aqui
@ActiveProfiles("test")
public class WebhookSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClickSignWebhookService webhookService;

    @MockBean
    private ClickSignHmacValidator hmacValidator;

    @Test
    void shouldAllowWebhookAccessWithoutAuthentication() throws Exception {
        // Simular HMAC válido
        when(hmacValidator.isValid(any(), any())).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhook/clicksign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"event\": \"test\"}")
                        .header("X-Clicksign-Hmac-Sha256", "some-hmac"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyProtectedPathWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/v1/signatures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
