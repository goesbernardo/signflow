package com.signflow.infrastructure.provider.docusign.docusign_exception;

import lombok.Data;

@Data
public class DocuSignErrorResponse {
    private String errorCode;
    private String message;
}
