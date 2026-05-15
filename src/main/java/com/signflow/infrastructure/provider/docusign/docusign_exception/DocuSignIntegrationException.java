package com.signflow.infrastructure.provider.docusign.docusign_exception;

import com.signflow.infrastructure.exception.IntegrationException;
import lombok.Getter;

@Getter
public class DocuSignIntegrationException extends IntegrationException {

    private final String dsErrorCode;

    public DocuSignIntegrationException(String message, String dsErrorCode, String rawResponse) {
        super(message, rawResponse);
        this.dsErrorCode = dsErrorCode;
    }

    public DocuSignIntegrationException(String message, String dsErrorCode, String rawResponse, Throwable cause) {
        super(message, rawResponse, cause);
        this.dsErrorCode = dsErrorCode;
    }
}
