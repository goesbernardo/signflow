package com.signflow.adapter.clicksign.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

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

            System.out.println("CLICK SIGN RAW RESPONSE:");
            System.out.println(rawBody);

            ObjectMapper mapper = new ObjectMapper();
            ClickSignErrorResponse errorResponse = mapper.readValue(rawBody, ClickSignErrorResponse.class);

            return new ClickSignIntegrationException("Erro na integração com Clicksign", errorResponse.getErrors(), rawBody);

        } catch (Exception e) {
            return new ClickSignIntegrationException("Erro ao processar resposta da Clicksign", List.of(), rawBody);
        }
    }


}
