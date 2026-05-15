package com.signflow.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signflow.infrastructure.provider.docusign.docusign_exception.DocuSignErrorDecoder;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import feign.optionals.OptionalDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Configuração Feign exclusiva do cliente DocuSign.
 * Não anotada com @Configuration para evitar registro global dos beans —
 * é referenciada apenas via @FeignClient(configuration = DocuSignFeignConfig.class).
 *
 * Autenticação: Bearer token (OAuth2 Access Token).
 * Para produção, implemente DocuSignTokenService com JWT Bearer Grant
 * (RSA private key) conforme documentação DocuSign:
 * https://developers.docusign.com/platform/auth/jwt-get-token/
 */
@Slf4j
public class DocuSignFeignConfig {

    @Value("${signflow.providers.docusign.access-token:}")
    private String accessToken;

    @Bean
    public feign.Client docuSignFeignClient() {
        okhttp3.OkHttpClient okHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        return new feign.okhttp.OkHttpClient(okHttpClient);
    }

    @Bean
    public RequestInterceptor docuSignRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + accessToken);
            requestTemplate.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            requestTemplate.header("Accept", MediaType.APPLICATION_JSON_VALUE);
        };
    }

    @Bean
    public Decoder docuSignFeignDecoder() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(objectMapper);
        converter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));

        return new ResponseEntityDecoder(new OptionalDecoder(
                new SpringDecoder(() -> new HttpMessageConverters(converter))));
    }

    @Bean
    public ErrorDecoder docuSignErrorDecoder(ObjectMapper objectMapper) {
        return new DocuSignErrorDecoder(objectMapper);
    }

    @Bean
    public Logger.Level docuSignFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
