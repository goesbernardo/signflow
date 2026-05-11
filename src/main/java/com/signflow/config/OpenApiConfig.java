package com.signflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Configuração da documentação OpenAPI / Swagger UI do SignFlow.
 * <p>
 * URLs de acesso:
 *   Local: <a href="http://localhost:8080/swagger-ui.html">...</a>
 *   Produção: <a href="https://signflow.api.br/swagger-ui.html">...</a>
 * <p>
 * JSON da spec:
 *   Local: <a href="http://localhost:8080/api-docs">...</a>
 *   Produção: <a href="https://signflow.api.br/api-docs">...</a>
 * <p>
 * Para habilitar/desabilitar em produção:
 *   Variável SWAGGER_ENABLED=true|false na AWS
 *   Default: true (visível para onboarding e integração)
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Value("${swagger.server-url:}")
    private String swaggerServerUrl;

    @Bean
    public OpenAPI signFlowOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(buildInfo())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildSecurityScheme()))
                .tags(buildTags());

        if (isValidHttpUrl(swaggerServerUrl)) {
            openAPI.setServers(List.of(
                    new Server()
                            .url(swaggerServerUrl)
                            .description("Servidor principal")
            ));
        }

        return openAPI;
    }

    private Info buildInfo() {
        return new Info()
                .title("SignFlow API")
                .description("""
                        ## Gateway de Assinatura Eletrônica Multi-Provider
                        
                        O SignFlow é uma API unificada que abstrai múltiplos provedores de assinatura eletrônica.
                        Integre uma vez e use ClickSign, D4Sign, DocuSign e outros sem mudar seu código.
                        
                        ### Autenticação
                        Todas as rotas (exceto `/auth/login` e `/webhook/**`) exigem JWT Bearer Token.
                        
                        1. Faça `POST /v1/auth/login` com `username` e `password`
                        2. Copie o token retornado
                        3. Clique em **Authorize** e cole: `Bearer {token}`
                        
                        ### Provider
                        Informe o header `provider` em todas as rotas de envelope:
                        - `CLICKSIGN` — padrão atual
                        - `D4SIGN` — em breve
                        - `DOCUSIGN` — em breve
                        
                        ### Fluxo padrão
                        Use `POST /create-activate-envelope` para criar, configurar e ativar em uma chamada.
                        """)
                .version("v1.0")
                .contact(new Contact()
                        .name("SignFlow")
                        .email("contato@signflow.com.br"))
                .license(new License()
                        .name("Proprietário")
                        .url("https://signflow.com.br"));
    }

    private SecurityScheme buildSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT obtido via POST /v1/auth/login");
    }

    private List<Tag> buildTags() {
        return List.of(
                new Tag().name("Autenticação")
                        .description("Login e obtenção do JWT token"),
                new Tag().name("Envelopes")
                        .description("Criação, listagem, ativação e cancelamento de envelopes"),
                new Tag().name("Documentos")
                        .description("Gestão de documentos associados ao envelope"),
                new Tag().name("Signatários")
                        .description("Gestão de signatários e lembretes"),
                new Tag().name("Requisitos")
                        .description("Requisitos de qualificação e autenticação por signatário"),
                new Tag().name("Observadores")
                        .description("Adicionar observadores que acompanham o envelope sem assinar"),
                new Tag().name("Webhook")
                        .description("Recebimento de eventos dos providers e histórico de entregas outbound"),
                new Tag().name("WhatsApp")
                        .description("Aceite de termos via WhatsApp sem documento PDF")
        );
    }

    private boolean isValidHttpUrl(String url) {
        if (!StringUtils.hasText(url)) return false;
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