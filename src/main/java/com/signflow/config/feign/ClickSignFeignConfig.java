package com.signflow.config.feign;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClickSignFeignConfig {

    @Value("${clicksign.api.token}")
    private String token;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", token);
            requestTemplate.header("Content-Type", "application/vnd.api+json");
            requestTemplate.header("Accept", "application/vnd.api+json");
        };
    }
}
