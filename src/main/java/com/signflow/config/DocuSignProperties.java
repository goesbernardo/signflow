package com.signflow.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "signflow.providers.docusign")
public class DocuSignProperties {

    private boolean enabled = false;
    private String baseUrl;
    private String accessToken;
    private String webhookSecret;
    private String integrationKey;
    private String secretKey;
    private String userId;
    private String accountId;
    private String keypairId;
    private String authServer = "https://account.docusign.com";

    /** PEM-encoded RSA private key (PKCS#8 ou PKCS#1). Env: DOCUSIGN_PRIVATE_KEY */
    private String privateKey;

    public boolean hasJwtCredentials() {
        return isNotBlank(integrationKey)
                && isNotBlank(userId)
                && isNotBlank(privateKey);
    }

    public boolean hasStaticToken() {
        return isNotBlank(accessToken);
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
