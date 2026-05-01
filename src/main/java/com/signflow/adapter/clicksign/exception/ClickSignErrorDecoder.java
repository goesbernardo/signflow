package com.signflow.adapter.clicksign.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClickSignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {

        String rawBody = null;

        try {
            if (response.body() != null) {
                rawBody = Util.toString(response.body().asReader());
            }

            log.error("Erro na integração com ClickSign: {}", rawBody);
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ClickSignErrorResponse errorResponse = mapper.readValue(rawBody, ClickSignErrorResponse.class);

            if (errorResponse != null && errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                return new ClickSignIntegrationException("Erro na integração com Clicksign", errorResponse.getErrors(), rawBody);
            }

            return new ClickSignIntegrationException("Erro na integração com Clicksign", List.of(), rawBody);

        } catch (Exception e) {
            return new ClickSignIntegrationException("Erro ao processar resposta da Clicksign", List.of(), rawBody);
        }
    }


}
