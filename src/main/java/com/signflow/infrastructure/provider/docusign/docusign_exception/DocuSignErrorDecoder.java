package com.signflow.infrastructure.provider.docusign.docusign_exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocuSignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public DocuSignErrorDecoder(ObjectMapper objectMapper) {
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

            log.error("Erro na integração com DocuSign. Status: {}. Method: {}. Body: {}", status, methodKey, rawBody);

            if (rawBody == null || rawBody.isBlank()) {
                if (status == 401) {
                    return new DocuSignIntegrationException(
                            "Erro de autenticação com o DocuSign. Verifique o access token. Status: " + status,
                            "UNAUTHORIZED", rawBody);
                }
                return new DocuSignIntegrationException(
                        "Erro na integração com DocuSign (corpo vazio). Status: " + status,
                        "EMPTY_RESPONSE", rawBody);
            }

            DocuSignErrorResponse errorResponse = objectMapper.readValue(rawBody, DocuSignErrorResponse.class);

            if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isBlank()) {
                String code = errorResponse.getErrorCode() != null ? errorResponse.getErrorCode() : "DOCUSIGN_ERROR";
                return new DocuSignIntegrationException(errorResponse.getMessage(), code, rawBody);
            }

            return new DocuSignIntegrationException(
                    "Erro na integração com DocuSign. Status: " + status,
                    "DOCUSIGN_ERROR", rawBody);

        } catch (Exception e) {
            log.error("Erro ao decodificar resposta de erro do DocuSign. Status: {}. Body: {}", status, rawBody, e);
            return new DocuSignIntegrationException(
                    "Erro ao processar resposta do DocuSign. Status: " + status,
                    "DECODE_ERROR", rawBody, e);
        }
    }
}
