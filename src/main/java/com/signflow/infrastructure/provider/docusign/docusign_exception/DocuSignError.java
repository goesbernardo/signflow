package com.signflow.infrastructure.provider.docusign.docusign_exception;

import lombok.Data;

@Data
public class DocuSignError {
    private String errorCode;
    private String message;
}
