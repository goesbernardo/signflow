package com.signflow.adapter.docusign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.signflow.infrastructure.provider.docusign.docusign_exception.DocuSignErrorDecoder;
import com.signflow.infrastructure.provider.docusign.docusign_exception.DocuSignIntegrationException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DocuSignErrorDecoder")
class DocuSignErrorDecoderTest {

    private DocuSignErrorDecoder decoder;

    @BeforeEach
    void setUp() {
        decoder = new DocuSignErrorDecoder(new ObjectMapper());
    }

    @Test
    @DisplayName("deve decodificar resposta de erro DocuSign com errorCode e message")
    void deveDecodificarRespostaDeErroDocuSign() {
        String json = "{\"errorCode\":\"ENVELOPE_DOES_NOT_EXIST\",\"message\":\"The specified envelope does not exist.\"}";
        var response = createResponse(404, json);

        var exception = decoder.decode("GET /envelopes/id", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getMessage()).isEqualTo("The specified envelope does not exist.");
        assertThat(dsEx.getDsErrorCode()).isEqualTo("ENVELOPE_DOES_NOT_EXIST");
    }

    @Test
    @DisplayName("deve retornar UNAUTHORIZED quando status for 401 e corpo vazio")
    void deveRetornarUnauthorizedParaStatus401SemCorpo() {
        var response = createResponse(401, "");

        var exception = decoder.decode("POST /envelopes", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getDsErrorCode()).isEqualTo("UNAUTHORIZED");
        assertThat(dsEx.getMessage()).contains("autenticação");
    }

    @Test
    @DisplayName("deve retornar EMPTY_RESPONSE quando corpo vazio e status != 401")
    void deveRetornarEmptyResponseParaCorpoVazio() {
        var response = createResponse(500, "");

        var exception = decoder.decode("POST /envelopes", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getDsErrorCode()).isEqualTo("EMPTY_RESPONSE");
    }

    @Test
    @DisplayName("deve retornar DOCUSIGN_ERROR quando errorCode não está presente no JSON")
    void deveRetornarDocuSignErrorQuandoErrorCodeAusente() {
        String json = "{\"errorCode\":null,\"message\":\"Algum erro\"}";
        var response = createResponse(400, json);

        var exception = decoder.decode("POST /envelopes", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getDsErrorCode()).isEqualTo("DOCUSIGN_ERROR");
    }

    @Test
    @DisplayName("deve retornar DOCUSIGN_ERROR quando JSON não tem o campo message")
    void deveRetornarDocuSignErrorQuandoMessageAusente() {
        String json = "{\"errorCode\":\"SOME_CODE\"}";
        var response = createResponse(422, json);

        var exception = decoder.decode("POST /envelopes", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getDsErrorCode()).isEqualTo("DOCUSIGN_ERROR");
    }

    @Test
    @DisplayName("deve retornar DECODE_ERROR quando JSON for inválido")
    void deveRetornarDecodeErrorParaJsonInvalido() {
        var response = createResponse(500, "{invalid-json}");

        var exception = decoder.decode("POST /envelopes", response);

        assertThat(exception).isInstanceOf(DocuSignIntegrationException.class);
        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getDsErrorCode()).isEqualTo("DECODE_ERROR");
    }

    @Test
    @DisplayName("deve preservar o rawResponse no exception")
    void devePreservarRawResponse() {
        String json = "{\"errorCode\":\"INVALID_REQUEST\",\"message\":\"Request inválido\"}";
        var response = createResponse(400, json);

        var exception = decoder.decode("POST /envelopes", response);

        var dsEx = (DocuSignIntegrationException) exception;
        assertThat(dsEx.getRawResponse()).isEqualTo(json);
    }

    private Response createResponse(int status, String body) {
        return Response.builder()
                .status(status)
                .reason("Reason")
                .request(Request.create(
                        Request.HttpMethod.POST, "/api",
                        Map.of(), null, StandardCharsets.UTF_8, null))
                .body(body.isEmpty() ? null : body, StandardCharsets.UTF_8)
                .headers(new LinkedHashMap<>())
                .build();
    }
}
