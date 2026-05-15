package com.signflow.infrastructure.provider.docusign.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signflow.config.DocuSignProperties;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gerencia o access token OAuth2 DocuSign via JWT Bearer Grant (RFC 7523).
 *
 * Fluxo:
 *   1. Cria um JWT assinado com a RSA private key (RS256)
 *   2. POST para {auth-server}/oauth/token com grant_type=jwt-bearer
 *   3. Armazena o token em cache e renova 5 minutos antes do vencimento
 *
 * Fallback: se não houver private-key configurada, usa o access-token estático.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "signflow.providers.docusign", name = "enabled", havingValue = "true")
public class DocuSignTokenService {

    private static final String JWT_GRANT_TYPE =
            "urn:ietf:params:oauth:grant-type:jwt-bearer";
    private static final String SCOPE = "signature impersonation";
    private static final int TOKEN_TTL_MINUTES = 55;   // DocuSign expira em 60 min
    private static final int JWT_EXPIRY_MINUTES = 1;   // JWT de curta duração para o grant

    private final DocuSignProperties props;
    private final RestTemplate restTemplate = new RestTemplate();

    private final AtomicReference<CachedToken> cache = new AtomicReference<>();

    public String getAccessToken() {
        if (props.hasJwtCredentials()) {
            return getOrRefreshJwtToken();
        }
        if (props.hasStaticToken()) {
            log.debug("Usando access-token estático configurado para DocuSign");
            return props.getAccessToken();
        }
        log.warn("DocuSign sem credenciais configuradas (private-key e access-token ausentes)");
        return "";
    }

    private String getOrRefreshJwtToken() {
        CachedToken cached = cache.get();
        if (cached != null && !cached.isExpired()) {
            return cached.token();
        }
        String token = requestNewToken();
        cache.set(new CachedToken(token, Instant.now().plus(TOKEN_TTL_MINUTES, ChronoUnit.MINUTES)));
        return token;
    }

    private String requestNewToken() {
        try {
            String assertion = buildJwt();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", JWT_GRANT_TYPE);
            body.add("assertion", assertion);

            String tokenUrl = props.getAuthServer().replaceAll("/$", "") + "/oauth/token";
            log.info("Solicitando access token DocuSign via JWT Bearer Grant: {}", tokenUrl);

            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    URI.create(tokenUrl),
                    new HttpEntity<>(body, headers),
                    TokenResponse.class);

            if (response.getBody() == null || response.getBody().accessToken() == null) {
                throw new IllegalStateException("Resposta vazia do endpoint de token DocuSign");
            }

            log.info("Access token DocuSign obtido com sucesso via JWT Bearer Grant");
            return response.getBody().accessToken();

        } catch (Exception e) {
            log.error("Falha ao obter access token DocuSign via JWT Bearer Grant", e);
            throw new RuntimeException("Não foi possível autenticar com o DocuSign: " + e.getMessage(), e);
        }
    }

    private String buildJwt() throws Exception {
        PrivateKey privateKey = parsePrivateKey(props.getPrivateKey());
        Instant now = Instant.now();

        // aud deve ser somente o host, sem https://
        String aud = props.getAuthServer()
                .replace("https://", "")
                .replace("http://", "")
                .replaceAll("/$", "");

        return Jwts.builder()
                .issuer(props.getIntegrationKey())
                .subject(props.getUserId())
                .audience().add(aud).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(JWT_EXPIRY_MINUTES, ChronoUnit.MINUTES)))
                .claim("scope", SCOPE)
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private PrivateKey parsePrivateKey(String pem) throws Exception {
        String cleaned = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] der = Base64.getDecoder().decode(cleaned);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(der);
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    private record CachedToken(String token, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private record TokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType,
            @JsonProperty("expires_in") Long expiresIn
    ) {}
}
