package com.signflow.adapter.clicksign.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ClickSignErrorDecoderTest {

    private ClickSignErrorDecoder decoder;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        decoder = new ClickSignErrorDecoder(objectMapper);
    }

    @Test
    void shouldExtractSpecificErrorMessageFromClickSign() {
        String json = """
                {
                    "errors": [
                        {
                            "detail": "Email inválido",
                            "code": "invalid_email",
                            "status": 422
                        }
                    ]
                }
                """;
        
        Response response = Response.builder()
                .status(422)
                .reason("Unprocessable Entity")
                .request(Request.create(Request.HttpMethod.POST, "/api/v1/envelopes", Map.of(), null, StandardCharsets.UTF_8, null))
                .body(json, StandardCharsets.UTF_8)
                .build();

        Exception exception = decoder.decode("createEnvelope", response);

        assertTrue(exception instanceof ClickSignIntegrationException);
        ClickSignIntegrationException clickSignEx = (ClickSignIntegrationException) exception;
        
        assertEquals("Email inválido", clickSignEx.getMessage());
        assertFalse(clickSignEx.getErrors().isEmpty());
        assertEquals("Email inválido", clickSignEx.getErrors().get(0).getDetail());
    }

    @Test
    void shouldFallbackToGenericMessageIfDetailIsEmpty() {
        String json = """
                {
                    "errors": [
                        {
                            "code": "unknown_error",
                            "status": 500
                        }
                    ]
                }
                """;

        Response response = Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.POST, "/api/v1/envelopes", Map.of(), null, StandardCharsets.UTF_8, null))
                .body(json, StandardCharsets.UTF_8)
                .build();

        Exception exception = decoder.decode("createEnvelope", response);

        assertTrue(exception instanceof ClickSignIntegrationException);
        ClickSignIntegrationException clickSignEx = (ClickSignIntegrationException) exception;

        assertEquals("Erro na integração com Clicksign", clickSignEx.getMessage());
    }

    @Test
    void shouldHandle401AuthErrorSpecifically() {
        Response response = Response.builder()
                .status(401)
                .reason("Unauthorized")
                .request(Request.create(Request.HttpMethod.POST, "/api/v1/envelopes", Map.of(), null, StandardCharsets.UTF_8, null))
                .body("", StandardCharsets.UTF_8)
                .build();

        Exception exception = decoder.decode("createEnvelope", response);

        assertTrue(exception instanceof ClickSignIntegrationException);
        ClickSignIntegrationException clickSignEx = (ClickSignIntegrationException) exception;

        assertTrue(clickSignEx.getMessage().contains("Erro de autenticação"));
        assertTrue(clickSignEx.getMessage().contains("401"));
    }
}
