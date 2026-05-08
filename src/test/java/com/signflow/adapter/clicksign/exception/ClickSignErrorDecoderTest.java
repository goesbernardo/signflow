package com.signflow.infrastructure.provider.clicksign.clicksign_exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
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
    void shouldDecodeValidationErrors() {
        String json = "{\"errors\":[{\"code\":\"100\",\"detail\":\"email inválido\",\"source\":{\"pointer\":\"/data/attributes/email\"}}]}";
        Response response = createResponse(422, json);

        Exception exception = decoder.decode("method", response);

        assertTrue(exception instanceof ClickSignIntegrationException);
        ClickSignIntegrationException ex = (ClickSignIntegrationException) exception;
        assertEquals("email inválido", ex.getMessage());
        assertEquals(1, ex.getErrors().size());
        ClickSignError error = ex.getErrors().get(0);
        assertEquals("100", error.getCode());
        assertEquals("email inválido", error.getDetail());
        assertEquals("/data/attributes/email", error.getSource().getPointer());
    }

    private Response createResponse(int status, String body) {
        return Response.builder()
                .status(status)
                .reason("Reason")
                .request(Request.create(Request.HttpMethod.POST, "/api", Map.of(), null, StandardCharsets.UTF_8, null))
                .body(body, StandardCharsets.UTF_8)
                .headers(new LinkedHashMap<>())
                .build();
    }
}
