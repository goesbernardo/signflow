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

            // Respostas não-JSON (HTML, texto) — detectar antes de tentar parse
            if (!looksLikeJson(rawBody)) {
                String code = resolveCodeFromStatus(status);
                String message = resolveMessageFromStatus(status, rawBody);
                return new DocuSignIntegrationException(message, code, rawBody);
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
                    "PARSE_ERROR", rawBody, e);
        }
    }

    private boolean looksLikeJson(String body) {
        if (body == null) return false;
        String trimmed = body.stripLeading();
        return trimmed.startsWith("{") || trimmed.startsWith("[");
    }

    private String resolveCodeFromStatus(int status) {
        return switch (status) {
            case 400 -> "INVALID_REQUEST";
            case 401 -> "UNAUTHORIZED";
            case 403 -> "FORBIDDEN";
            case 404 -> "NOT_FOUND";
            case 409 -> "CONFLICT";
            case 429 -> "RATE_LIMITED";
            default  -> "DOCUSIGN_ERROR";
        };
    }

    private String resolveMessageFromStatus(int status, String rawBody) {
        return switch (status) {
            case 400 -> "Requisição inválida para o DocuSign. Verifique o accountId e os parâmetros.";
            case 401 -> "Não autorizado no DocuSign. Verifique o access token.";
            case 403 -> "Sem permissão para executar esta operação no DocuSign.";
            case 404 -> "Recurso não encontrado no DocuSign.";
            case 429 -> "Rate limit atingido no DocuSign.";
            default  -> "Erro na integração com DocuSign. Status: " + status;
        };
    }
}
