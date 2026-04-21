package com.signflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @org.springframework.beans.factory.annotation.Value("${swagger.server-url:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI signFlowOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("SignFlow API")
                        .description("Documentacao das APIs de assinatura e integracao com provedores.")
                        .version("v1")
                        .contact(new Contact()
                                .name("Time SignFlow"))
                        .license(new License()
                                .name("Proprietary")));

        if (isValidHttpUrl(swaggerServerUrl)) {
            openAPI.setServers(List.of(new Server().url(swaggerServerUrl)));
        }

        return openAPI;
    }

    private boolean isValidHttpUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return false;
        }

        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && uri.getHost() != null;
        } catch (URISyntaxException ex) {
            return false;
        }
    }
}
