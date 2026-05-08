package com.signflow.adapter.clicksign.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ClickSignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public ClickSignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {

        String rawBody = null;
        int status = response.status();

        try {
            if (response.body() != null) {
                rawBody = Util.toString(response.body().asReader());
            }

            log.error("Erro na integração com ClickSign. Status: {}. Method: {}. RawBody: {}", status, methodKey, rawBody);

            if (rawBody == null || rawBody.isBlank()) {
                if (status == 401) {
                    return new ClickSignIntegrationException("Erro de autenticação com a ClickSign. Verifique o token da API. Status: " + status, List.of(), rawBody);
                }
                return new ClickSignIntegrationException("Erro na integração com Clicksign (corpo vazio). Status: " + status, List.of(), rawBody);
            }

            ClickSignErrorResponse errorResponse = objectMapper.readValue(rawBody, ClickSignErrorResponse.class);

            if (errorResponse != null && errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                String message = errorResponse.getErrors().get(0).getDetail();
                if (message == null || message.isBlank()) {
                    message = "Erro na integração com Clicksign";
                }
                return new ClickSignIntegrationException(message, errorResponse.getErrors(), rawBody);
            }

            return new ClickSignIntegrationException("Erro na integração com Clicksign. Status: " + status, List.of(), rawBody);

        } catch (Exception e) {
            log.error("Erro ao decodificar resposta de erro da ClickSign. Status: {}. RawBody: {}", status, rawBody, e);
            return new ClickSignIntegrationException("Erro ao processar resposta da Clicksign. Status: " + status, List.of(), rawBody, e);
        }
    }


}
